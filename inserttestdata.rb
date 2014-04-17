
require './lib/database/dbfarmbot'

Mongoid.load!("config/mongo.yml", :development)

FarmBot.where(:active => true).order_by([:name,:asc]).each do |farmbot|
  farmbot.delete
end

bot = FarmBot.new
bot.crops = Array.new
bot.name = 'BOT1'
bot.active = true
bot.uuid = "063df52b-0698-4e1c-b2bb-4c0890019782"


crop = Crop.new
crop.crop_id = 'test001'
crop.plant_type = 'carrot'
crop.coord_x = 5
crop.coord_y = 7
crop.coord_z = 3
crop.radius = 0.5
crop.status = 'GROWING'
crop.date_at_planting = DateTime.parse('2014/01/01')
crop.age_at_planting = 0
crop.age_at_fully_grown = 120
crop.age_at_harvest = 150
crop.valid_data = true

grc = GrowCoefficient.new
grc.age_in_percentage = 0
grc.amount_water_manual = 15
crop.grow_coefficients << grc

grc = GrowCoefficient.new
grc.age_in_percentage = 50
grc.amount_water_manual = 25
crop.grow_coefficients << grc

grc = GrowCoefficient.new
grc.age_in_percentage = 100
grc.amount_water_manual = 75
crop.grow_coefficients << grc

grc = GrowCoefficient.new
grc.age_in_percentage = 200
grc.amount_water_manual = 65
crop.grow_coefficients << grc


wat = Watering.new
wat.time = '19:00'
wat.percentage = 100
crop.waterings << wat

bot.crops << crop
bot.save


FarmBot.where(:active => true).order_by([:name,:asc]).each do |farmbot|
  puts farmbot.inspect
  farmbot.crops.each do |crop|
    puts crop.inspect
  end
end
