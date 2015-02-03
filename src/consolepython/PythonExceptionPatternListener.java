package consolepython;

import java.net.URL;

import org.eclipse.debug.ui.console.IConsole;
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

public class PythonExceptionPatternListener implements IPatternMatchListener {
    private static final String header = "(Traceback \\(most recent call last\\):)?";
    private static final String causes = "(\\s+File\\s+\"[^\"]+\",\\s+line\\s+\\d+(,\\s+in[^\\n]+\\n)?";
    private static final String exception = "[^\\n]+\\n)+\\s*(\\w+):([^\\n]+)";
    
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
		return null ;
	}
	
	public void consoleWriter(IConsole console, final String exception) {
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				Shell shell;
				shell = new Shell(Display.getDefault());
				String text = "";
				
				text += "A solution was found to resolve your error:\n\n"+exception.replace("\n", "");
				text += "\nDo you want to open it ?\n";
				text += "(http://stackoverflow.com/questions/271625/)";
				if (MessageDialog.openQuestion(shell, "ErrorResolver", text)) {
					IWorkbenchBrowserSupport support = PlatformUI.getWorkbench().getBrowserSupport(); 
					try {
						IWebBrowser browser = support.createBrowser("ExceptionResolver"); 
						browser.openURL(new URL("http://stackoverflow.com/questions/271625/"));
					} catch (Exception	 e) {
					}
				}
			}
		});
	}
	
	@Override
	public void matchFound(PatternMatchEvent event) {
		IConsole console = (IConsole)event.getSource();

		System.out.println("Exception found");
		System.out.println(this.getException(console, event));
		this.consoleWriter(console, this.getException(console, event));
	}

	@Override
	public String getPattern() {
		return header+causes+exception;
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
