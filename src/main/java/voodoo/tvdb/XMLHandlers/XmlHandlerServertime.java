package voodoo.tvdb.XMLHandlers;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class XmlHandlerServertime extends DefaultHandler {
	
	private boolean in_servertime = false;
	
	String time;
	
	/**
	 * Getter
	 */
	public String getTime(){
		return time;
	}
	
	/**
	 * Methods
	 */
	@Override
	public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException{
		
		if(localName.equalsIgnoreCase("servertime")){
			this.in_servertime = true;
		}
		
	}
	
	@Override
	public void endElement(String namespaceURI, String localName, String qNames) throws SAXException{

		if(localName.equalsIgnoreCase("servertime")){
			this.in_servertime = false;
		}
		
	}
	
	@Override
	public void characters(char ch[], int start, int length){
		String chars = new String(ch, start, length);
		
		if(this.in_servertime){
			time = time == null ? chars : time + chars;
		}
	}
}
