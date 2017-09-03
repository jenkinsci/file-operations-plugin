package sp.sd.fileoperations.dsl;

import hudson.Extension;
import javaposse.jobdsl.dsl.RequiresPlugin;
import javaposse.jobdsl.dsl.helpers.step.StepContext;
import javaposse.jobdsl.plugin.ContextExtensionPoint;
import javaposse.jobdsl.plugin.DslExtensionMethod;
import sp.sd.fileoperations.FileOperationsBuilder;

@Extension(optional = true)
public class FileOperationsJobDslExtension extends ContextExtensionPoint {
    @RequiresPlugin(id = "file-operations", minimumVersion = "1.2")
    @DslExtensionMethod(context = StepContext.class)
    public Object fileOperations(Runnable closure) {
        FileOperationsJobDslContext context = new FileOperationsJobDslContext();
        executeInContext(closure, context);
        return new FileOperationsBuilder(context.fileOperations);
    }
}
