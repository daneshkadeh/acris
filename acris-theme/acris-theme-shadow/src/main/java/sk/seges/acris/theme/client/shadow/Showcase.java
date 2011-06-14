package sk.seges.acris.theme.client.shadow;

import sk.seges.acris.widget.client.form.ImageCheckBox;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;


public class Showcase implements EntryPoint {

	@Override
	public void onModuleLoad() {
		Button button = (Button)new ShadowButtonPanel();
		button.setText("sss");
		RootPanel.get().add(button);
		
		TextBox tb = (TextBox)new ShadowTextBoxPanel();
		RootPanel.get().add(tb);
		tb = (TextBox)new ShadowTextBoxPanel();
		RootPanel.get().add(tb);
		CheckBox cb = (CheckBox)new ShadowCheckBoxPanel();
		cb.setText("Testik");
		RootPanel.get().add(cb);

		cb = (CheckBox)new ShadowCheckBoxPanel();
		cb.setEnabled(false);
		RootPanel.get().add(cb);

		cb = (CheckBox)new ShadowCheckBoxPanel();
		cb.setEnabled(false);
		cb.setValue(true);
		RootPanel.get().add(cb);

		cb = (CheckBox)new ImageCheckBox();
		RootPanel.get().add(cb);
		cb = (CheckBox)new ImageCheckBox();
		cb.setValue(true);
		cb.setEnabled(false);
		RootPanel.get().add(cb);

		cb = (CheckBox)new ImageCheckBox();
		cb.setValue(false);
		cb.setEnabled(false);
		RootPanel.get().add(cb);

		cb = (CheckBox)new ImageCheckBox();
		cb.setValue(false);
		cb.setEnabled(false);
		RootPanel.get().add(cb);

		cb = (CheckBox)new ImageCheckBox();
		cb.setValue(true);
		cb.setEnabled(false);
		RootPanel.get().add(cb);
	}
}