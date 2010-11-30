package sk.seges.acris.generator.client.presenter;

import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.EventBus;
import com.gwtplatform.mvp.client.RootPresenter;

public class RootMoviePresenter extends RootPresenter {

	public static class RootMovieView extends RootView {

		@Override
		public void setInSlot(Object slot, Widget content) {
			RootPanel rootPanel = RootPanel.get("ratings");
			rootPanel.add(content);
		}
	}

	@Inject
	public RootMoviePresenter(EventBus eventBus, RootMovieView view) {
		super(eventBus, view);
	}
}