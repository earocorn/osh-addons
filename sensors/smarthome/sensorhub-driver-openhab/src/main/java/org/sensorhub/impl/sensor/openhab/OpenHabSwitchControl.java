package org.sensorhub.impl.sensor.openhab;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;


import org.sensorhub.api.command.CommandException;
import org.sensorhub.api.sensor.SensorException;
import org.sensorhub.impl.sensor.AbstractSensorControl;
import org.vast.data.DataChoiceImpl;
import org.vast.swe.SWEHelper;

import net.opengis.swe.v20.DataBlock;
import net.opengis.swe.v20.DataChoice;
import net.opengis.swe.v20.DataComponent;
import net.opengis.swe.v20.Text;


public class OpenHabSwitchControl extends AbstractSensorControl<OpenHabDriver>
{
	DataChoice commandData;

	public OpenHabSwitchControl(OpenHabDriver driver)
	{
		super(driver.getName(), driver);
	}

	
	@Override
	public String getName()
	{
		return "switchControl";
	}

	
	protected void init()
	{
//		System.out.println("Adding Switch Control SWE Template");
		
		SWEHelper sweHelp = new SWEHelper();
		commandData = sweHelp.newDataChoice();
		commandData.setName(getName());
		
		Text turnOn = sweHelp.newText("http://sensorml.com/ont/swe/property/turnOn", 
        		"On", 
        		"Set switch On");
		commandData.addItem("setOn", turnOn);
		
		Text turnOff = sweHelp.newText("http://sensorml.com/ont/swe/property/turnOff", 
        		"Off", 
        		"Set switch Off");
		commandData.addItem("setOff", turnOff);
	}
	
	
	@Override
	public DataComponent getCommandDescription() {
		return commandData;
	}


    protected void start() throws SensorException
    {
    }
	
    
    @Override
	public boolean execCommand(DataBlock command) throws CommandException {
    	
    	// associate command data to msg structure definition
        DataChoice commandMsg = (DataChoice) commandData.copy();
        commandMsg.setData(command);
        DataComponent component = ((DataChoiceImpl) commandMsg).getSelectedItem();
        
        String indexName = component.getName();
        String cmd = "";
        
        DataBlock data = component.getData();
        String name = data.getStringValue(0);
        
        if (indexName.equalsIgnoreCase("setOn"))
        	cmd = "ON";
        else if (indexName.equalsIgnoreCase("setOff"))
        	cmd = "OFF";
    	
        
        // make http post
        try
        {
			URL optionsURL = new URL(parentSensor.getHostURL() + 
					"/items/" + name);
			HttpURLConnection conn = (HttpURLConnection) optionsURL.openConnection();
			conn.setDoOutput(true);
			conn.addRequestProperty("Content-Type", "text/plain");
			conn.addRequestProperty("Accept", "application/json");
			conn.setRequestMethod("POST");
			conn.connect();
			
			byte[] outputBytes = cmd.getBytes("UTF-8");
			OutputStream os = conn.getOutputStream();
			
			System.out.println("trying request " + optionsURL + " with " + cmd);
			os.write(outputBytes);
			conn.getResponseCode();
			os.flush();
			os.close();
		}
        catch (Exception e)
        {
        	throw new CommandException("Error sending command", e);
		}
        

        
        return false;
	}
    
    
    protected void stop()
    {       
    }

}
