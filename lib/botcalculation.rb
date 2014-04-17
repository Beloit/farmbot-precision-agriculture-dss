# Calculate a schedule based on plant coefficient

require_relative './database/dbcommand'

class BotScheduleCalculation

  def initialize
    Mongoid.load!("config/mongo.yml", :development)
  end

  # loop all bots to see if any need a revised schedule
  def calculate_all_bots()
    FarmBot.where(:active => true).order_by([:name,:asc]).each do |farmbot|
	  puts "Checking bot #{farmbot.name}"
      calculate_bot_schedule(farmbot)
    end
  end

  # make a schedule for each crop
  def calculate_bot_schedule(farmbot)

    farmbot.crops.where(:valid_data => true).each do |crop|

      puts "crop type=#{crop.plant_type} @ x=#{crop.coord_x} y=#{crop.coord_y}"

      # retrieve the right coefficient for the crop
      coefficient = get_coefficient_for_day(crop, Time.now)

      # for each separate watering to do for this crop, create a bot action in the schedule
      crop.waterings.each do |watering|
      
        create_watering_schedule(crop, watering, coefficient)
		
      end
    end

    # save the data
    farmbot.save
	   
  end
    
  def create_watering_schedule(crop, watering, coefficient)

    puts "watering percentage = #{watering.percentage}"
    puts "watering time       = #{watering.time}"
	
    # add a new item to the schedule
    new_item = ScheduledCommand.new
    new_item.scheduled_time = watering.time
    new_item.crop_id = crop._id
    new_item.schedule_id = watering._id
	
    # retract the tools
    new_action = ScheduledCommandLine.new
    new_action.action = "HOME Z"
    new_item.scheduled_command_lines << new_action
	
    # move to the right position for watering
    new_action = ScheduledCommandLine.new
    new_action.action = "MOVE"
    new_action.coord_x = crop.coord_x - crop.radius
    new_action.coord_y = crop.coord_y
    new_action.coord_z = 0
    new_item.scheduled_command_lines << new_action
	
    # move to the watering position
    new_action = ScheduledCommandLine.new
    new_action.action = "MOVE"
    new_action.coord_x = crop.coord_x - crop.radius
    new_action.coord_y = crop.coord_y
    new_action.coord_z = crop.coord_z
    new_item.scheduled_command_lines << new_action
	
    # move to the watering position
    new_action = ScheduledCommandLine.new
    new_action.action = "WATER"
    new_action.amount = watering.percentage * coefficient
    new_item.scheduled_command_lines << new_action
	
    # retract the tools
    new_action = ScheduledCommandLine.new
    new_action.action = "HOME Z"
    new_item.scheduled_command_lines << new_action

    # add the data
    crop.scheduled_commands << new_item

  end
	
  def get_coefficient_for_day(crop, given_day)
  
    # calculate the age of the crop in percent to make it possible to retrieve the coefficient
  
    age_in_percent = 0
    age_crop_in_days = given_day - crop.date_at_planting - crop.age_at_planting

    puts "age at fully grown = #{crop.age_at_fully_grown}"
    puts "age at harvest     = #{crop.age_at_harvest}"
    
    if crop.age_at_fully_grown > 0 and crop.age_at_fully_grown > 0
    
      if age_crop_in_days <= crop.age_at_fully_grown
        age_in_percent = age_crop_in_days / crop.age_at_fully_grown
      end

      if age_crop_in_days > crop.age_at_fully_grown and age_crop_in_days < crop.age_at_harvest
          age_in_percent = age_crop_in_days / crop.age_at_harvest
      end

      if crop.age_at_harvest > age_crop_in_days
        age_in_percent = 200
      end
    end

    puts "age in percent = #{age_in_percent}"
    
    # get the lower and higher coefficient for the current age of the crop, so the ones just next to the current percentage
	
    lower_coef = crop.grow_coefficients.where(:age_in_percentage.lte => age_in_percent).order_by([:age_in_percentage,:desc]).first
    upper_coef = crop.grow_coefficients.where(:age_in_percentage.gt  => age_in_percent).order_by([:age_in_percentage,:asc] ).first
    
    puts "lower amount percentage/water = #{lower_coef.age_in_percentage}% => #{lower_coef.amount_water_manual}"
    puts "upper amount percentage/water = #{upper_coef.age_in_percentage}% => #{upper_coef.amount_water_manual}"

	# calculate the new coefficient as interpolation between the lower and upper coefficient
    new_coef = lower_coef.amount_water_manual + (upper_coef.amount_water_manual - lower_coef.amount_water_manual) * (upper_coef.age_in_percentage - lower_coef.age_in_percentage) * (age_in_percent - lower_coef.age_in_percentage) / 100

    puts "new coefficient = #{new_coef}"
	
	return new_coef
	
  end
    
end