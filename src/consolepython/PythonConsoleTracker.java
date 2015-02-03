package consolepython;

import org.eclipse.debug.ui.console.IConsole;
import org.eclipse.debug.ui.console.IConsoleLineTracker;
import org.eclipse.debug.ui.console.IConsoleLineTrackerExtension;
import org.eclipse.jface.text.IRegion;

public class PythonConsoleTracker implements IConsoleLineTracker {
	private IConsole console;
	
	@Override
	public void init(IConsole console) {
		this.console = console;
		System.out.println(console.toString());
		this.console.addPatternMatchListener(new PythonExceptionPatternListener());
	}

	@Override
	public void lineAppended(IRegion line) {
	}

	@Override
	public void dispose() {
	}
}
