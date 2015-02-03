package consolejava;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.eclipse.debug.ui.console.IConsole;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
import org.eclipse.ui.console.IPatternMatchListener;
import org.eclipse.ui.console.PatternMatchEvent;
import org.eclipse.ui.console.TextConsole;

public class JavaExceptionPatternListener implements IPatternMatchListener {
	private static final String clazz = "((?:[\\w\\s](?:\\$+|\\.|/)?)+)";
	private static final String method = "\\.([\\w|_|\\$|\\s|<|>]+)";
	private static final String exceptionClazz = "((?:\\w(?:\\$+|\\.|/)?)+)";
	private static final String exception = "(" + exceptionClazz + "(?:Exception|Error))";
	private static final String sourceChars = "[^\\(\\)]+";
	private static final String source = "\\((" + sourceChars + "(?:\\([^\\)]*\\))?)\\)";
	private static final String frame = "(?:\\s*at\\s+)" + clazz + method + "\\s*" + source;
	private static final String cause = "(((?:\\s*...\\s+\\d+\\s+more)?\\s+Caused\\s+by:\\s+)" + exception+")?";
	
	@Override
	public void connect(TextConsole console) {
	}

	@Override
	public void disconnect() {
	}

	public String getException(IConsole console, PatternMatchEvent event) {
		int start = event.getOffset();
		int length = event.getLength();
		String exception;
		try {
			exception = console.getDocument().get(start, length);
			return exception;
		} catch (BadLocationException e) {
		}
		return null;
	}
	
	public String callAPI(String exception) throws IOException {
		String url = "http://172.18.13.101:8084/web-stax/stack";
		URL obj = new URL(url);
		HttpURLConnection connexion = (HttpURLConnection) obj.openConnection();
		
		connexion.setRequestMethod("POST");
		connexion.setRequestProperty("User-Agent", "Mozilla/5.0");
		connexion.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
 
		String urlParameters = "stacktrace=\""+exception+"\"";

		connexion.setDoOutput(true);
		DataOutputStream wr = new DataOutputStream(connexion.getOutputStream());
		wr.writeBytes(urlParameters);
		wr.flush();
		wr.close();
 
		int responseCode = connexion.getResponseCode();
		System.out.println("\nSending 'POST' request to URL : " + url);
		System.out.println("Post parameters : " + urlParameters);
		System.out.println("Response Code : " + responseCode);
 
		BufferedReader in = new BufferedReader(new InputStreamReader(connexion.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();
 
		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();
 
		System.out.println(response.toString());
		return response.toString().replace("[", "").replace("]", "").split(";")[0];
	}

	public void consoleWriter(IConsole console, final String exception, final String postId) {
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				Shell shell;
				shell = new Shell(Display.getDefault());
				String text = "";
				
				text += "A solution was found to resolve your error:\n\n"+exception+"\n";
				text += "\nDo you want to open it ?\n";
				text += "http://stackoverflow.com/questions/" + postId;
				MessageDialog md = new MessageDialog(shell, "ErrorResolver", null, text, 0, new String[]{IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL, "More"}, 0);
				IWorkbenchBrowserSupport support = PlatformUI.getWorkbench().getBrowserSupport(); 
				switch(md.open()) {
					case 0:
						try {
							IWebBrowser browser = support.createBrowser("ExceptionResolver"); 
							browser.openURL(new URL("http://stackoverflow.com/questions/"+postId));
						} catch (Exception	 e) {
						}
						break;
					case 1:
						break;
					case 2:
						try {
							IWebBrowser browser = support.createBrowser("ExceptionResolver"); 
							browser.openURL(new URL("http://172.18.13.101:8084/web-stax?stacktrace="+exception));
						} catch (Exception	 e) {
						}
						break;
				}
			}
		});
	}
	
	@Override
	public void matchFound(PatternMatchEvent event) {
		IConsole console = (IConsole)event.getSource();
		
		System.out.println("found");
		String exception = this.getException(console, event);
		String result = null;
		try {
			result = this.callAPI(exception);
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (result != null && result.compareTo("") != 0) {
			this.consoleWriter(console, exception, result);
		}
	}

	@Override
	public String getPattern() {
		return ".*(Exception|Error):?[^\\n]+\\n+(\\s*at[^\\n]+)+";
//		return exception+frame+cause;
	}

	@Override
	public int getCompilerFlags() {
		return 0;
	}

	@Override
	public String getLineQualifier() {
		return null;
	}

}
