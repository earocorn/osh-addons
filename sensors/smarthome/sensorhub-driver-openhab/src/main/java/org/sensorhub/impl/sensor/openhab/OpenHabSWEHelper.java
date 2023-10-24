package org.sensorhub.impl.sensor.openhab;

import org.vast.swe.SWEConstants;
import org.vast.swe.SWEHelper;
import org.vast.swe.helper.GeoPosHelper;

import net.opengis.swe.v20.DataType;
import net.opengis.swe.v20.Quantity;
import net.opengis.swe.v20.Text;
import net.opengis.swe.v20.Vector;

public class OpenHabSWEHelper
{
//	public Text getNameSWE()
//	{
//    	SWEHelper sweHelpName = new SWEHelper();
//
//    	Text name = sweHelpName.newText("http://sensorml.com/ont/swe/property/SensorName",
//        		"Sensor Name", "Name of Sensor from OpenHAB");
//
//        return name;
//	}

	public Text getNameSWE()
	{
		SWEHelper sweHelpName = new SWEHelper();

		return sweHelpName.createText()
				.name("sensor-name")
				.label("Sensor Name")
				.definition("http://sensorml.com/ont/swe/property/SensorName")
				.description("Name of Sensor from OpenHAB")
				.build();
	}

//	public Quantity getBatteryLevelSWE()
//	{
//		SWEHelper sweHelpBatt = new SWEHelper();
//
//		Quantity battery = sweHelpBatt.newQuantity("http://sensorml.com/ont/swe/property/BatteryLevel",
//        		"Battery Level",
//        		"Battery Level of 'Thing'",
//        		"%", DataType.INT);
//
//		return battery;
//	}

	public Quantity getBatteryLevelSWE()
	{
		SWEHelper sweHelpBatt = new SWEHelper();

		return sweHelpBatt.createQuantity()
				.name("battery-level")
				.label("Battery Level")
				.definition("http://sensorml.com/ont/swe/property/BatteryLevel")
				.description("Battery Level of 'Thing'")
				.uomCode("%")
				.build();
	}
	
//	public Text getItemStateSWE()
//	{
//		SWEHelper sweHelpItemState = new SWEHelper();
//
//        Quantity itemState = sweHelpItemState.newQuantity("http://sensorml.com/ont/swe/property/ItemState",
//        		"Current State",
//        		"Current state of item",
//        		null, DataType.ASCII_STRING);
//
//        return itemState;
//	}

	public Text getItemStateSWE()
	{
		SWEHelper sweHelpItemState = new SWEHelper();

		return sweHelpItemState.createText()
				.name("current-state")
				.label("Current State")
				.definition("http://sensorml.com/ont/swe/property/ItemState")
				.description("Current state of item")
				.build();

	}
	
//	public Text getEnviroDataSWE()
//	{
//		SWEHelper sweHelpEnviroData = new SWEHelper();
//
//        Quantity enviroData = sweHelpEnviroData.newQuantity("http://sensorml.com/ont/swe/property/EnvironmentData",
//        		"Environment Data",
//        		"Current data value offered by environmental sensor",
//        		null, DataType.ASCII_STRING);
//
//        return enviroData;
//	}

	public Text getEnviroDataSWE()
	{
		SWEHelper sweHelpEnviroData = new SWEHelper();

		return sweHelpEnviroData.createText()
				.name("environment-data")
				.label("Environment Data")
				.definition("http://sensorml.com/ont/swe/property/EnvironmentData")
				.description("Current data value offered by environmental sensor")
				.build();
	}
	
//	public Quantity getTempSWE()
//	{
//		SWEHelper sweHelpTemp = new SWEHelper();
//
//		Quantity temp = sweHelpTemp.newQuantity("http://sensorml.com/ont/swe/property/Temperature",
//        		"Air Temperature",
//        		"Temperature of Surrounding Air",
//        		"Cel", DataType.DOUBLE);
//
//		return temp;
//	}

	public Quantity getTempSWE()
	{
		SWEHelper sweHelpTemp = new SWEHelper();

		return sweHelpTemp.createQuantity()
				.name("air-temperature")
				.label("Air temperature")
				.definition("http://sensorml.com/ont/swe/property/Temperature")
				.description("Temperature of Surrounding Air")
				.uomCode("Cel")
				.build();
	}
	
//	public Quantity getRelHumSWE()
//	{
//		SWEHelper sweHelpHum = new SWEHelper();
//
//		Quantity relhum = sweHelpHum.newQuantity("http://sensorml.com/ont/swe/property/RelativeHumidity",
//        		"Relative Humidity",
//        		"Relative Humidity",
//        		"%", DataType.DOUBLE);
//
//		return relhum;
//	}

	public Quantity getRelHumSWE()
	{
		SWEHelper sweHelpHum = new SWEHelper();

		return sweHelpHum.createQuantity()
				.name("relative-humidity")
				.label("Relative Humidity")
				.definition("http://sensorml.com/ont/swe/property/RelativeHumidity")
				.description("Relative Humidity")
				.uomCode("%")
				.build();
	}
	
//	public Quantity getLumSWE()
//	{
//		SWEHelper sweHelpLum = new SWEHelper();
//
//		Quantity lux = sweHelpLum.newQuantity("http://sensorml.com/ont/swe/property/Illuminance",
//        		"Illuminance",
//        		"Luminous Flux per Area",
//        		"lx", DataType.FLOAT);
//
//		return lux;
//	}

	public Quantity getLumSWE()
	{
		SWEHelper sweHelpLum = new SWEHelper();

		return sweHelpLum.createQuantity()
				.name("illuminance")
				.label("Illuminance")
				.definition("http://sensorml.com/ont/swe/property/Illuminance")
				.description("Luminous Flux per Area")
				.uomCode("lx")
				.build();
	}
	
//	public Quantity getUVISWE()
//	{
//		SWEHelper sweHelpUV = new SWEHelper();
//
//		Quantity uvi = sweHelpUV.newQuantity("http://sensorml.com/ont/swe/property/UVI",
//        		"UV Index",
//        		"Index of Ultraviolet Radiation",
//        		"UVI", DataType.INT);
//
//		return uvi;
//	}

	public Quantity getUVISWE()
	{
		SWEHelper sweHelpUV = new SWEHelper();

		return sweHelpUV.createQuantity()
				.name("uv-index")
				.label("UV Index")
				.definition("http://sensorml.com/ont/swe/property/UVI")
				.description("Index of Ultraviolet Radiation")
//				.uomCode("uvi")
				.build();
	}
	
//	public Quantity getThingStatusSWE()
//	{
//		SWEHelper sweHelpStatus = new SWEHelper();
//
//		Quantity status = sweHelpStatus.newQuantity("http://sensorml.com/ont/swe/property/ThingStatus",
//        		"Thing Status",
//        		"Status of Thing",
//        		null, DataType.ASCII_STRING);
//
//		return status;
//	}

	public Text getThingStatusSWE()
	{
		SWEHelper sweHelpStatus = new SWEHelper();

		return sweHelpStatus.createText()
				.name("thing-status")
				.label("Thing Status")
				.definition("http://sensorml.com/ont/swe/property/ThingStatus")
				.description("Status of Thing")
				.build();
	}

//	public Text getSwitchStateSWE()
//	{
//		SWEHelper sweHelpSwitch = new SWEHelper();
//
//		Quantity state = sweHelpSwitch.newQuantity("http://sensorml.com/ont/swe/property/SwitchState",
//        		"Switch Status",
//        		"Status of Switch",
//        		null, DataType.ASCII_STRING);
//
//		return state;
//	}

	public Text getSwitchStateSWE()
	{
		SWEHelper sweHelpSwitch = new SWEHelper();

		return sweHelpSwitch.createText()
				.name("switch-status")
				.label("Switch Status")
				.definition("http://sensorml.com/ont/swe/property/SwitchState")
				.description("Status of Switch")
				.build();
	}
	
//	public Quantity getSensorBinarySWE()
//	{
//		SWEHelper sweHelpBinary = new SWEHelper();
//
//		Quantity status = sweHelpBinary.newQuantity("http://sensorml.com/ont/swe/property/SensorState",
//        		"Sensor Status",
//        		"Status of Sensor",
//        		null, DataType.ASCII_STRING);
//
//		return status;
//	}

	public Text getSensorBinarySWE()
	{
		SWEHelper sweHelpBinary = new SWEHelper();

		return sweHelpBinary.createText()
				.name("sensor-status")
				.label("Sensor Status")
				.definition("http://sensorml.com/ont/swe/property/SensorState")
				.description("Status of Sensor")
				.build();
	}
	
//	public Quantity getAlarmEntrySWE()
//	{
//		SWEHelper sweHelpEntry = new SWEHelper();
//
//		Quantity entry = sweHelpEntry.newQuantity("http://sensorml.com/ont/swe/property/Entry",
//        		"Alarm Status",
//        		"Status of Entry Alarm",
//        		null, DataType.ASCII_STRING);
//
//		return entry;
//	}

	public Text getAlarmEntrySWE()
	{
		SWEHelper sweHelpEntry = new SWEHelper();

		return sweHelpEntry.createText()
				.name("entry-alarm-status")
				.label("Alarm Status")
				.definition("http://sensorml.com/ont/swe/property/Entry")
				.description("Status of Entry Alarm")
				.build();
	}
	
//	public Quantity getAlarmBurglarSWE()
//	{
//		SWEHelper sweHelpBurglar = new SWEHelper();
//
//		Quantity burg = sweHelpBurglar.newQuantity("http://sensorml.com/ont/swe/property/Burglar",
//        		"Alarm Status",
//        		"Status of Burglar Alarm",
//        		null, DataType.ASCII_STRING);
//
//		return burg;
//	}

	public Text getAlarmBurglarSWE()
	{
		SWEHelper sweHelpBurglar = new SWEHelper();

		return sweHelpBurglar.createText()
				.name("burglar-alarm-status")
				.label("Alarm Status")
				.definition("http://sensorml.com/ont/swe/property/Burglar")
				.description("Status of Burglar Alarm")
				.build();
	}
	
//	public Quantity getAlarmGeneralSWE()
//	{
//		SWEHelper sweHelpGen = new SWEHelper();
//
//		Quantity gen = sweHelpGen.newQuantity("http://sensorml.com/ont/swe/property/General",
//        		"Alarm Status",
//        		"Status of General Alarm",
//        		null, DataType.ASCII_STRING);
//
//		return gen;
//	}

	public Text getAlarmGeneralSWE()
	{
		SWEHelper sweHelpGeneral = new SWEHelper();

		return sweHelpGeneral.createText()
				.name("general-alarm-status")
				.label("Alarm Status")
				.definition("http://sensorml.com/ont/swe/property/General")
				.description("Status of General Alarm")
				.build();
	}
	
//	public Quantity getAlarmAllSWE()
//	{
//		SWEHelper sweHelpAlarm = new SWEHelper();
//
//		Quantity alarm = sweHelpAlarm.newQuantity("http://sensorml.com/ont/swe/property/Alarm",
//        		"Alarm Status",
//        		"Status of All Alarms",
//        		null, DataType.ASCII_STRING);
//
//		return alarm;
//	}

	public Text getAlarmAllSWE()
	{
		SWEHelper sweHelpAlarm = new SWEHelper();

		return sweHelpAlarm.createText()
				.name("all-alarm-status")
				.label("Alarm Status")
				.definition("http://sensorml.com/ont/swe/property/Alarm")
				.description("Status of All Alarms")
				.build();
	}
	
//	public Quantity getMotionStatusSWE()
//	{
//		SWEHelper sweHelpMotion = new SWEHelper();
//
//		Quantity motion = sweHelpMotion.newQuantity("http://sensorml.com/ont/swe/property/MotionStatus",
//        		"Motion Status",
//        		"Status of Motion Switch",
//        		null, DataType.ASCII_STRING);
//
//		return motion;
//	}

	public Text getMotionStatusSWE()
	{
		SWEHelper sweHelpMotion = new SWEHelper();

		return sweHelpMotion.createText()
				.name("motion-status")
				.label("Motion Status")
				.definition("http://sensorml.com/ont/swe/property/MotionStatus")
				.description("Status of Motion Switch")
				.build();
	}
	
//	public Quantity getSetLevelSWE()
//	{
//		SWEHelper sweHelpSetLevel = new SWEHelper();
//
//		Quantity level = sweHelpSetLevel.newQuantity("http://sensorml.com/ont/swe/property/SetLevel",
//        		"Set Level",
//        		"Level of Selector Switch",
//        		"%", DataType.INT);
//
//		return level;
//	}

	public Quantity getSetLevelSWE()
	{
		SWEHelper sweHelpSetLevel = new SWEHelper();

		return sweHelpSetLevel.createQuantity()
				.name("set-level")
				.label("Set Level")
				.definition("http://sensorml.com/ont/swe/property/SetLevel")
				.description("Level of Selector Switch")
				.uomCode("%")
				.build();
	}
	
	public Vector getLocVecSWE()
	{
		GeoPosHelper posHelp = new GeoPosHelper();
		
//		Vector locVector = posHelp.newLocationVectorLLA(SWEConstants.DEF_SENSOR_LOC);
//        locVector.setLabel("Location");
//        locVector.setDescription("Location LLA input by user");
//
//        return locVector;


		return posHelp.createLocationVectorLLA()
				.name("location")
				.label("Location")
//				.definition()
				.description("Location LLA input by user")
				.build();

	}

//	public Text getLocDescSWE()
//	{
//		SWEHelper sweHelpLocDesc = new SWEHelper();
//
//        Text locDesc = sweHelpLocDesc.newText("http://sensorml.com/ont/swe/property/LocationDescription",
//        		"Location Description", "Sensor Location Description");
//
//        return locDesc;
//	}
//
	public Text getLocDescSWE()
	{
		SWEHelper sweHelpLocDesc = new SWEHelper();

		return sweHelpLocDesc.createText()
				.name("location-description")
				.label("Location Description")
				.definition("http://sensorml.com/ont/swe/property/LocationDescription")
				.description("Sensor Location Description")
				.build();
	}
	
//	public Text getBindingTypeSWE()
//	{
//		SWEHelper sweHelpBindingType = new SWEHelper();
//
//        Text type = sweHelpBindingType.newText("http://sensorml.com/ont/csm/property/BindingType",
//        		"Binding Type", "Type of Channel given by OpenHAB");
//
//        return type;
//	}

	public Text getBindingTypeSWE()
	{
		SWEHelper sweHelpBindingType = new SWEHelper();

		return sweHelpBindingType.createText()
				.name("binding-type")
				.label("Binding Type")
				.definition("http://sensorml.com/ont/csm/property/BindingType")
				.description("Type of Channel give by OpenHAB")
				.build();
	}
	
//	public Quantity getOwningThingSWE()
//	{
//		SWEHelper sweHelpOwningThing = new SWEHelper();
//
//        Quantity thing = sweHelpOwningThing.newQuantity("http://sensorml.com/ont/csm/property/OwningThing",
//        		"Owning Thing",
//        		"'Thing' that owns item",
//        		null, DataType.ASCII_STRING);
//
//        return thing;
//	}

	public Text getOwningThingSWE()
	{
		SWEHelper sweHelpOwningThing = new SWEHelper();

		return sweHelpOwningThing.createText()
				.name("owning-thing")
				.label("Owning Thing")
				.definition("http://sensorml.com/ont/csm/property/OwningThing")
				.description("'Thing' that owns item")
				.build();
	}
	
//	public Text getAlertMsgSWE()
//	{
//		SWEHelper sweHelpAlert = new SWEHelper();
//
//        Text alert = sweHelpAlert.newText("http://sensorml.com/ont/csm/property/AlertMessage",
//        		"Alert Message", "Alert Message for Domoticz device");
//
//        return alert;
//	}
//
	public Text getAlertMsgSWE()
	{
		SWEHelper sweHelpAlert = new SWEHelper();

		return sweHelpAlert.createText()
				.name("alert-message")
				.label("Alert Message")
				.definition("http://sensorml.com/ont/csm/property/AlertMessage")
				.description("Alert Message for Domoticz device")
				.build();
	}
	
	// Continue
	// .
	// .
	// .
}
