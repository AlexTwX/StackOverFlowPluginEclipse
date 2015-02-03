package consolejava;

import org.eclipse.debug.ui.console.IConsole;
import org.eclipse.debug.ui.console.IConsoleLineTracker;
import org.eclipse.jface.text.IRegion;

public class JavaConsoleTracker implements IConsoleLineTracker	{
	private IConsole console;
	
	@Override
	public void init(IConsole console) {
		this.console = console;
		this.console.addPatternMatchListener(new JavaExceptionPatternListener());
	}

	@Override
	public void lineAppended(IRegion line) {
	}

	@Override
	public void dispose() {
	}
}
