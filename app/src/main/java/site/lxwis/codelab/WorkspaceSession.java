package site.lxwis.codelab;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class WorkspaceSession {
    private final String project;
    private final List<String> tabs;
    private final String active;

    WorkspaceSession(String project, List<String> tabs, String active) {
        this.project = project;
        this.tabs = Collections.unmodifiableList(new ArrayList<>(tabs));
        this.active = active;
    }

    public String getProject() { return project; }
    public List<String> getTabs() { return tabs; }
    public String getActive() { return active; }
}
