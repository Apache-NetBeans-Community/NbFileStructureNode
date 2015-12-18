package org.chrisle.netbeans.plugins.nbfilestructurenode;

import com.sun.source.tree.ClassTree;
import com.sun.source.util.TreePathScanner;
import java.io.IOException;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import javax.swing.JOptionPane;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.Task;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.MIMEResolver;
import org.openide.loaders.DataNode;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiFileLoader;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.FilterNode.Children;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author chrl
 */
@NbBundle.Messages({
    "LBL_InstallPluginAction_LOADER=Java file"
})
@MIMEResolver.ExtensionRegistration(
        displayName = "#LBL_InstallPluginAction_LOADER",
        mimeType = "text/x-java",
        extension = {"java", "JAVA"}
)
@DataObject.Registration(
        mimeType = "text/x-java",
        displayName = "#LBL_InstallPluginAction_LOADER",
        position = 300
)
@ActionReferences({
    @ActionReference(
            path = "Loaders/text/x-java/Actions",
            id = @ActionID(category = "Edit", id = "org.openide.actions.CutAction"),
            position = 300
    ),
    @ActionReference(
            path = "Loaders/text/x-java/Actions",
            id = @ActionID(category = "Edit", id = "org.openide.actions.CopyAction"),
            position = 400,
            separatorAfter = 500
    ),
    @ActionReference(
            path = "Loaders/text/x-java/Actions",
            id = @ActionID(category = "Edit", id = "org.openide.actions.DeleteAction"),
            position = 600
    ),
    @ActionReference(
            path = "Loaders/text/x-java/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.RenameAction"),
            position = 700,
            separatorAfter = 800
    ),
    @ActionReference(
            path = "Loaders/text/x-java/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.SaveAsTemplateAction"),
            position = 900,
            separatorAfter = 1000
    ),
    @ActionReference(
            path = "Loaders/text/x-java/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.FileSystemAction"),
            position = 1100,
            separatorAfter = 1200
    ),
    @ActionReference(
            path = "Loaders/text/x-java/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.ToolsAction"),
            position = 1300
    ),
    @ActionReference(
            path = "Loaders/text/x-java/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.PropertiesAction"),
            position = 1400
    )
})
public class ExtendedJavaDataObject extends MultiDataObject {

    public ExtendedJavaDataObject(FileObject fo, MultiFileLoader loader) throws DataObjectExistsException {
        super(fo, loader);
    }

    @Override
    protected Node createNodeDelegate() {
        DataNode dataNode = new DataNode(this, Children.create(new JavaChildFactory(this), true), getLookup());
//        dataNode.getI

//        dataNode.setIconBaseWithExtension(FileUtil.getConfigObject("text/x-java", ExtendedJavaDataObject.class));
        return dataNode;
    }

    @Override
    public Lookup getLookup() {
        return getCookieSet().getLookup();
    }

    private static class JavaChildFactory extends ChildFactory<String> {

        private final ExtendedJavaDataObject dObj;

        public JavaChildFactory(ExtendedJavaDataObject dObj) {
            this.dObj = dObj;
        }

        @Override
        protected boolean createKeys(List list) {
            FileObject fObj = dObj.getPrimaryFile();

            JavaSource js = JavaSource.forFileObject(fObj);

            try {
                js.runUserActionTask(new Task<CompilationController>() {
                    public void run(CompilationController parameter) throws IOException {
                        parameter.toPhase(Phase.ELEMENTS_RESOLVED);
                        new MemberVisitor(parameter).scan(parameter.getCompilationUnit(), null);
                    }
                }, true);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }

//            try {
//                List<String> dObjContent = fObj.asLines();
//                list.addAll(dObjContent);
//            } catch (IOException ex) {
//                Exceptions.printStackTrace(ex);
//            }
            return true;
        }

        @Override
        protected Node createNodeForKey(String key) {
            Node childNode = new AbstractNode(Children.LEAF);
            childNode.setDisplayName(key);
            return childNode;
        }
    }

    private static class MemberVisitor extends TreePathScanner<Void, Void> {

        private final CompilationInfo info;

        public MemberVisitor(CompilationInfo info) {
            this.info = info;
        }

        @Override
        public Void visitClass(ClassTree t, Void v) {
            Element el = info.getTrees().getElement(getCurrentPath());

            if (el == null) {
                System.err.println("Cannot resolve class!");
            } else {
                TypeElement te = (TypeElement) el;

                List<ExecutableElement> methodsIn = ElementFilter.methodsIn(te.getEnclosedElements());
                
                JOptionPane.showMessageDialog(null, methodsIn.size());
                
                for (ExecutableElement executableElement : methodsIn) {
                    JOptionPane.showMessageDialog(null, executableElement.getSimpleName().toString());
                }

                System.err.println("Resolved class: " + te.getQualifiedName().toString());
                //XXX: only as an example, uses toString on element, which should be used only for debugging
                System.err.println("enclosed methods: " + ElementFilter.methodsIn(te.getEnclosedElements()));
                System.err.println("enclosed types: " + ElementFilter.typesIn(te.getEnclosedElements()));
            }
            return null;
        }
    }
}