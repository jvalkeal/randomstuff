package com.example.demo;

import org.springframework.shell.Command.Help;
import org.springframework.shell.ConfigurableCommandRegistry;
import org.springframework.shell.MethodTarget;
import org.springframework.shell.standard.AbstractShellComponent;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

@ShellComponent
public class DemoCommands extends AbstractShellComponent {

	@ShellMethod(key = "register", value = "Register dump command")
	public void register() {
		MethodTarget hiCommand = MethodTarget.of("hiCommand", this, new Help("hi command", null));
		((ConfigurableCommandRegistry)getCommandRegistry()).register("hi", hiCommand);
	}

	@SuppressWarnings("unused")
	private String hiCommand() {
		return "hi";
	}
}
