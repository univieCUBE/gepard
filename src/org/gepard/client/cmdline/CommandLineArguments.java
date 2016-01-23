package org.gepard.client.cmdline;

import java.util.Arrays;
import java.util.HashMap;

public class CommandLineArguments {
	
	private HashMap<String,String> values;
	
	
	public CommandLineArguments(String allowed, String[] args) throws InvalidArgumentsException {
		this(allowed.split(","), args);
	}

	public CommandLineArguments(String[] allowed, String[] args) throws InvalidArgumentsException {

		// initialize
		values = new HashMap<String,String>();
		Arrays.sort(allowed);
		
		// parse command line arguments
		for (int i=0; i<args.length; i++) {
			String argument = args[i];

			// we only allow arguments starting with -
			if (! (argument.charAt(0) == '-')) 
				throw new InvalidArgumentsException("Invalid argument: '" + argument 
						+ "'... argument names must start with -");
		
			// extract real key name
			String key = argument.substring(1).trim();
			
			// if the next argument does NOT start with a - => this is the value
			String value = "";
			if (i+1<args.length && !args[i+1].startsWith("-")) {
				value = args[i+1];
				i++;
			}
			
			// valid?
			if (Arrays.binarySearch(allowed, key) < 0)
				throw new InvalidArgumentsException("Unknown argument: '" + key + "'"); 

			// store in hashmap
			values.put(key, value);
		}
		
	}

	public boolean isSet(String switchName) {
		return values.get(switchName) != null;
	}
	
	public String getValue(String key) {
		return values.get(key);
	}
	
	
}
