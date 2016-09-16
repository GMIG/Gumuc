package gumuc1;

import org.apache.log4j.Logger;

    import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

    import fi.iki.elonen.NanoHTTPD;

    public class AttendServer extends NanoHTTPD {

    	private static final Logger logger = Logger.getLogger(AttendServer.class);
    	@SuppressWarnings("unused")
		private final Exposition expo;
    	
    	private static HashMap<Integer,Consumer<Void>> expoCommands = new HashMap<Integer,Consumer<Void>>();
    	
        public AttendServer(Exposition expo) throws IOException {
            super(80);
            start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
            logger.info("Starting Attender Server");
            this.expo = expo;
    		expoCommands.put(1, (Void)->expo.powerAllUp()); //Включить экспозицию
    		expoCommands.put(2, (Void)->expo.getBrix().get(0).restart());
    		expoCommands.put(3, (Void)->expo.getBrix().get(1).restart());
    		expoCommands.put(4, (Void)->expo.getBrix().get(3).restart());
    		expoCommands.put(5, (Void)->expo.loadAndRunDatatons());
    		expoCommands.put(6, (Void)->expo.powerAllDown());
    		expoCommands.put(7, (Void)->expo.powerUpRelay());
            expoCommands.put(0, (Void)->expo.refresh());
            //System.out.println("\nRunning! Point your browers to http://localhost:8080/ \n");
        }

//        public static void main(String[] args) {
//            try {
//                new AttendServer();
//            } catch (IOException ioe) {
//                System.err.println("Couldn't start server:\n" + ioe);
//            }
//        }

        @Override
        public Response serve(IHTTPSession session) {
            Map<String, List<String>> parms ;
                        
            InputStream is=session.getInputStream();
            
           byte [] bytes = new byte[20];int i=0;
            try {
				while(is.available()>0){
					bytes[i++]=(byte)(is.read());
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
            parms = decodeParameters(new String(bytes));
            logger.info("AttendServer received body " + parms);

            String codeRef = "1717";
            if(!parms.containsKey("code") ){
				try {
		            Map<String, String> files = new HashMap<String, String> ();
					session.parseBody(files);
		            if(!files.containsKey("postData"))
		                return fromPage();

					String filesString = files.get("postData");
		            logger.info("AttendServer Received files " + filesString);
		            String codeMaybe = filesString.substring(5, 5+4);
		            String indexMaybe = filesString.substring(17, 17+1);
		            System.out.println(codeMaybe);
		            System.out.println(indexMaybe);

    	            if(codeMaybe != null && codeMaybe.equals(codeRef)){
    	            	try{
    		            	Integer ind = Integer.parseInt(indexMaybe);
    		            	expoCommands.get(ind).accept(null);
    	            	}
    	            	catch(NumberFormatException e){
    	            		return fromPage();
    	            	}
    	            }
				} catch (Exception e) {
					//e.printStackTrace();
            		return fromPage();

				}
            }
            else{
                try{    
                    String code =  parms.containsKey("code") ?parms.get("code").get(0).substring(0, 4):null;  
	            	logger.info("code " + code);

        	            if(code != null && code.equals(codeRef)){
        		            	Integer ind = Integer.parseInt(parms.get("code").get(0).substring(12,13));
        		            	logger.info("ind " + ind);

        		            	expoCommands.get(ind).accept(null);
        	            }
                    }
                    catch(Exception e){
                		return fromPage();
                    }
            }

            return fromPage();
            //if (parms.get("username") == null) {
        }
       
        
    
    
    
   private Response fromPage(){
       String msg = "<html><head><meta charset='utf-8'>"
       		+ "<style type='text/css'>"
       +  "button {"
       +  " 	display:block;"
      +  " 	height:57px;"
       +  "	line-height:57px;"
       +  "	width:150px;"
       +  "	text-decoration:none;"
       +  "	text-align:center;"
       + "		line-height: 1.5em;"
       +  " }"
       +  "</style></head>"
       + "<body><h1>Управление экспозицией</h1>\n";

       msg += "<form method='post' target='_top' enctype='text/plain'>";
       msg += "<label>Код: <input  type='text' name='code' value=''></input></label><p>";
       msg += "<button  type='submit' name='index' value='0'> Кнопка </button>";
       msg += "<button  type='submit' name='index' value='1'> Включить экспозицию </button>";
       msg += "<button  type='submit' name='index' value='2'> Перегрузить книгу памяти </button>";
       msg += "<button  type='submit' name='index' value='3'> Перегрузить звуки дверей </button>";
       msg += "<button  type='submit' name='index' value='4'> Перегрузить списки </button>";
       msg += "<button  type='submit' name='index' value='5'> Перезапустить видео на проекторах </button>";
       msg += "<button  type='submit' name='index' value='6'> Выключить экспозицию </button>";
       msg += "<button  type='submit' name='index' value='7'> Включить бараки </button>";

       msg +=  "</p>\n" + "</form>\n";
   //}
  

   return newFixedLengthResponse(msg + "</body></html>\n");
   }

   }