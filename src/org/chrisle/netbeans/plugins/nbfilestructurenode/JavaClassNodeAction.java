package org.chrisle.netbeans.plugins.nbfilestructurenode;

import java.awt.event.ActionEvent;
import java.util.List;
import javax.swing.AbstractAction;
import org.openide.cookies.LineCookie;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.Line;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.Utilities;

public final class JavaClassNodeAction extends AbstractAction {
    private Lookup context;

    public JavaClassNodeAction() {
        this(Utilities.actionsGlobalContext());
    }

    public JavaClassNodeAction(Lookup context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            JavaClassNode javaClassNodeLkp = this.context.lookup(JavaClassNode.class);
            FileObject primaryFile = context.lookup(ExtendedJavaDataObject.class).getPrimaryFile();
//                    getCookieSet().getLookup().lookup(ExtendedJavaDataObject.class).getPrimaryFile();
            DataObject dobj = DataObject.find(primaryFile);
            String name = javaClassNodeLkp.getDisplayName();

            dobj.getLookup().lookup(OpenCookie.class).open();

            LineCookie lc = dobj.getLookup().lookup(LineCookie.class);
            List<? extends Line> lines = lc.getLineSet().getLines();

            for (Line line : lines) {
                if (line.getText().contains(name)) {
                    line.show(Line.ShowOpenType.OPEN, Line.ShowVisibilityType.FRONT);
                }
            }
        } catch (DataObjectNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
}