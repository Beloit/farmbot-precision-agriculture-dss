
require './lib/database/dbfarmbot'
require './lib/botcalculation.rb'

Mongoid.load!("config/mongo.yml", :development)

FarmBot.where(:active => true).order_by([:name,:asc]).each do |farmbot|
  farmbot.crops.each do |crop|
    crop.scheduled_commands.each do |command|
	  command.delete
	end
  end
end



calc = BotScheduleCalculation.new
calc.calculate_all_bots()

puts ''

FarmBot.where(:active => true).order_by([:name,:asc]).each do |farmbot|
  farmbot.crops.each do |crop|
    puts "bot: #{farmbot.name}"
    crop.scheduled_commands.each do |command|
	  puts "  command scheduled at #{command.scheduled_time}"
	  command.scheduled_command_lines.each do |line|
	    puts "    do #{line.action} at #{line.coord_x} / #{line.coord_y} / #{line.coord_z} amount #{line.amount}"
	  end
	end
  end
end

