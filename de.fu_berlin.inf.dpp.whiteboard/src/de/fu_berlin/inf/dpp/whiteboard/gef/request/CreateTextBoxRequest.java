package de.fu_berlin.inf.dpp.whiteboard.gef.request;

import org.eclipse.gef.requests.CreateRequest;

import de.fu_berlin.inf.dpp.whiteboard.gef.editpolicy.XYLayoutWithFreehandEditPolicy;

public class CreateTextBoxRequest extends CreateRequest {

    protected String text;

    public CreateTextBoxRequest() {
        setType(XYLayoutWithFreehandEditPolicy.REQ_CREATE_TEXTBOX);
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

}
