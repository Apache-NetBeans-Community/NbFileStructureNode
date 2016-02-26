package org.chrisle.netbeans.plugins.nbfilestructurenode;

import static com.sun.glass.ui.Cursor.setVisible;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import org.openide.text.Line;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Element;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.Task;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.cookies.LineCookie;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.MIMEResolver;
import org.openide.loaders.DataNode;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiFileLoader;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.FilterNode.Children;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.util.Utilities;

/**
 *
 * @author ChrisLE
 */
@Messages({
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
            id = @ActionID(category = "System", id = "org.openide.actions.OpenAction"),
            position = 100,
            separatorAfter = 200
    ),
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

    public static List _list = new ArrayList();
    
    private static String classImage = "org/chrisle/netbeans/plugins/nbfilestructurenode/resources/class.png";
    private static String methodImage = "org/chrisle/netbeans/plugins/nbfilestructurenode/resources/method.png";

    public static Image getIconForElement(Element te) {
        Image result = null;

        if (null != te.getKind()) {
            switch (te.getKind()) {
                case CLASS:
                    if (te.getModifiers().contains(Modifier.ABSTRACT)) {
                        result = ImageUtilities.loadImage("org/chrisle/netbeans/plugins/nbfilestructurenode/resources/abstractClass.png");
                    } else if (te.getModifiers().contains(Modifier.FINAL)) {
                        result = ImageUtilities.mergeImages(ImageUtilities.loadImage(classImage),
                                ImageUtilities.loadImage("org/chrisle/netbeans/plugins/nbfilestructurenode/resources/finalMark_dark.png"), 0, 0);
//                    else if(te.getClass().isInstance(Exception.class)) {
//                        result = ImageUtilities.loadImage("org/chrisle/netbeans/plugins/nbfilestructurenode/resources/exceptionClass.png");
                    } else if(te.getModifiers().contains(Modifier.PUBLIC)) {
                        
                    } else {
                        result = ImageUtilities.loadImage(classImage);
                    }
                    break;
                case INTERFACE:
                    result = ImageUtilities.loadImage("org/chrisle/netbeans/plugins/nbfilestructurenode/resources/interface.png");
                    break;
                case ENUM:
                    result = ImageUtilities.loadImage("org/chrisle/netbeans/plugins/nbfilestructurenode/resources/enum.png");
                    break;
                case ANNOTATION_TYPE:
                    result = ImageUtilities.loadImage("org/chrisle/netbeans/plugins/nbfilestructurenode/resources/annotationtype.png");
                    break;
                case METHOD:
                    if (te.getModifiers().contains(Modifier.PUBLIC)) {
                        result = ImageUtilities.mergeImages(ImageUtilities.loadImage(methodImage),
                                ImageUtilities.loadImage("org/chrisle/netbeans/plugins/nbfilestructurenode/resources/public.png"), 15, 1);
                    } else {
                        result = ImageUtilities.loadImage(methodImage);
                    }
                    break;
                case FIELD:
                    result = ImageUtilities.loadImage("org/chrisle/netbeans/plugins/nbfilestructurenode/resources/field.png");
                    break;
                default:
                    break;
            }
        }

        return result;
    }
    
    public class ImageMerge {
 
    private BufferedImage mergeImage;
 
        /**
         *
         */
        private Image processImages(String image1, String image2) {
            try {
                // load source images
                BufferedImage image = ImageIO.read(new File(image1));
                BufferedImage overlay = ImageIO.read(new File(image2));

                // create the new image, canvas size is the max. of both image sizes
                int w = Math.max(image.getWidth(), overlay.getWidth());
                int h = Math.max(image.getHeight(), overlay.getHeight());
                BufferedImage combined = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

                // paint both images, preserving the alpha channels
                Graphics g = combined.getGraphics();
                g.drawImage(image, 0, 0, null);
                g.drawImage(overlay, 0, 0, null);

                // Save as new image
                ImageIO.write(combined, "PNG", new File("combined.png"));

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public ExtendedJavaDataObject(FileObject fo, MultiFileLoader loader) throws DataObjectExistsException {
        super(fo, loader);
        registerEditor("text/x-java", true);
    }

    @Override
    public Lookup getLookup() {
        return getCookieSet().getLookup();
    }
    
    private List<Element> _children;

    public List<Element> getChildren() {
        return _children;
    }

    public void setChildren(List<Element> _children) {
        this._children = _children;
    }

    @Override
    protected Node createNodeDelegate() {
        _children = getElementsFromFile(getPrimaryFile());
        Image iconForElement = null;

        if (!_children.isEmpty()) {
            Element firstElement = _children.iterator().next();
            iconForElement = getIconForElement(firstElement);
        }

        final Image icon = iconForElement;
        DataNode dataNode = new DataNode(this, Children.create(new JavaChildFactory(_children), true), getLookup()) {
            @Override
            public Image getIcon(int type) {
                if (null != icon) {
                    return icon;
                } else {
                    //show default ?
                    return ImageUtilities.loadImage("org/chrisle/netbeans/plugins/nbfilestructurenode/resources/class.png");
                }
            }

            @Override
            public Image getOpenedIcon(int type) {
                return getIcon(type);
            }
        };

        return dataNode;
    }

    private JavaSource _js;

    private List<Element> getElementsFromFile(FileObject fObj) throws IllegalArgumentException {
        final List<Element> result = new ArrayList<>();

        _js = JavaSource.forFileObject(fObj);

        if (_js == null) {
            return result;
        }

        try {
            _js.runUserActionTask(new Task<CompilationController>() {
                @Override
                public void run(CompilationController cc) throws Exception {
                    cc.toPhase(Phase.ELEMENTS_RESOLVED);

                    for (Element te : cc.getTopLevelElements()) {
                        result.add(te);
                    }
                }
            }, true);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

        return result;
    }

    private List<Element> typeElemenChilds = new ArrayList<>();

    private class JavaChildFactory extends ChildFactory<Element> {

        private final List<Element> elements;

        public JavaChildFactory(final List<Element> list) {
            this.elements = list;
        }

        @Override
        protected boolean createKeys(final List<Element> elements) {
            if (this.elements != null) {
                elements.addAll(this.elements);
            }

            return true;
        }

        @Override
        protected Node createNodeForKey(Element te) {
            JavaClassNode childNode = new JavaClassNode();
            childNode.setDisplayName(te.getSimpleName().toString());

            typeElemenChilds = (List<Element>) te.getEnclosedElements();

            Image icon = getIconForElement(te);
            if (icon != null) {
                childNode.icon = icon;
            }

            return childNode;
        }
    }

    private class JavaClassNode extends AbstractNode {

        Image icon;

        @Override
        public Image getIcon(int type) {
            return icon;
        }

        @Override
        public Action getPreferredAction() {
            return new JavaClassNodeAction();
        }

        public JavaClassNode() {
            super(Children.create(new JavaChildFactory(typeElemenChilds), true));
//            super(Children.LEAF);
        }
    }

    private final class JavaClassNodeAction extends AbstractAction {

        private Lookup context;
        Lookup.Result<JavaClassNode> lkpInfo;

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
                FileObject primaryFile = getCookieSet().getLookup().lookup(ExtendedJavaDataObject.class).getPrimaryFile();
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
}
