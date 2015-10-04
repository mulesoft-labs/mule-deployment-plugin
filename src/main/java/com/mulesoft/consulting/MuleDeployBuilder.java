package com.mulesoft.consulting;

import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.mule.tools.maven.plugin.mule.DeploymentException;
import org.mule.tools.maven.plugin.mule.cloudhub.CloudhubDeployer;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Sample {@link Builder}.
 * <p/>
 * <p/>
 * When the user configures the project and enables this builder,
 * {@link DescriptorImpl#newInstance(StaplerRequest)} is invoked
 * and a new {@link MuleDeployBuilder} is created. The created
 * instance is persisted to the project configuration XML by using
 * XStream, so this allows you to use instance fields
 * to remember the configuration.
 * <p/>
 * <p/>
 * When a build is performed, the {@link #perform(AbstractBuild, Launcher, BuildListener)}
 * method will be invoked.
 *
 * @author Adam Davis
 */
public class MuleDeployBuilder extends Builder {

    private final String username;
    private final String password;
    private final String environment;
    private final String applicationName;
    private final String application;
    private final String region;
    private final String muleVersion;
    private final String workers;
    private final String workerType;
    private final CloudhubProperty[] chProperties;


    /**
     * We'll use this from the <tt>config.jelly</tt>.
     */
    public String getUsername() {
        return username;
    }

    /**
     * We'll use this from the <tt>config.jelly</tt>.
     */
    public String getPassword() {
        return password;
    }

    /**
     * We'll use this from the <tt>config.jelly</tt>.
     */
    public String getEnvironment() {
        return environment;
    }

    /**
     * We'll use this from the <tt>config.jelly</tt>.
     */
    public String getApplicationName() {
        return applicationName;
    }

    /**
     * We'll use this from the <tt>config.jelly</tt>.
     */
    public String getApplication() {
        return application;
    }

    /**
     * We'll use this from the <tt>config.jelly</tt>.
     */
    public String getRegion() {
        return region;
    }

    /**
     * We'll use this from the <tt>config.jelly</tt>.
     */
    public String getMuleVersion() {
        return muleVersion;
    }

    /**
     * We'll use this from the <tt>config.jelly</tt>.
     */
    public String getWorkers() {
        return workers;
    }

    /**
     * We'll use this from the <tt>config.jelly</tt>.
     */
    public String getWorkerType() {
        return workerType;
    }

    /**
     * We'll use this from the <tt>config.jelly</tt>.
     */
    public CloudhubProperty[] getChProperties() {
        return chProperties;
    }

    // Fields in config.jelly must match the parameter names in the "DataBoundConstructor"
    @DataBoundConstructor
    public MuleDeployBuilder(String username, String password, String environment, String applicationName,
                             String application,
                             String region, String muleVersion, String workers, String workerType,
                             CloudhubProperty[] chProperties) {
        this.username = username;
        this.password = password;
        this.environment = environment;
        this.applicationName = applicationName;
        this.application = application;
        this.region = region;
        this.muleVersion = muleVersion;
        this.workers = workers;
        this.workerType = workerType;
        this.chProperties = chProperties;

    }

    @Override
    public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) {
        // This is where you 'build' the project.
        // Since this is a dummy, we just say 'hello world' and call that a build.

        EnvVars envVars = new EnvVars();
        Map<String, String> properties = new HashMap<String, String>();

        for (CloudhubProperty chp : chProperties)
            properties.put(chp.getKey(), chp.getValue());

        listener.getLogger().println("***MuleDeployPlugin: username:" + username + "\n  password:" + password
                + "\n  environment:" + environment + "\n  applicationName:" + applicationName
                + "\n  application:" + application + "\n  region:" + region
                + "\n  muleVersion:" + muleVersion + "\n  workerType:" + workerType
                + " properties:" + properties);

        boolean success = false;

        try {
            envVars = build.getEnvironment(listener);
            for (FilePath file : build.getWorkspace().list(this.application)) {
                 // This also shows how you can consult the global configuration of the builder
                try {
                    new CloudhubDeployer(username, password, hudson.Util.replaceMacro(environment, envVars)
                            , hudson.Util.replaceMacro(applicationName, envVars)
                            , new File(file.getRemote()), (region == null || region.length() == 0) ? null : region
                            , (muleVersion == null || muleVersion.length() == 0) ? null : muleVersion
                            , (workers == null || workers.length() == 0) ? null : new Integer(workers)
                            , (workerType == null || workerType.length() == 0) ? null : workerType
                            , new SystemStreamLog(),
                            properties).deploy();
                    success = true;
                } catch (DeploymentException e) {
                    e.printStackTrace();
                }
                //if multiple files match pattern only deploy the first
                break;
            }
        } catch (Exception e) {

        }
        return success;

    }

    // Overridden for better type safety.
    // If your plugin doesn't really define any property on Descriptor,
    // you don't have to do this.
    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }

    /**
     * Descriptor for {@link MuleDeployBuilder}. Used as a singleton.
     * The class is marked as public so that it can be accessed from views.
     * <p/>
     * <p/>
     * See <tt>src/main/resources/hudson/plugins/hello_world/MuleDeployBuilder/*.jelly</tt>
     * for the actual HTML fragment for the configuration screen.
     */
    @Extension // This indicates to Jenkins that this is an implementation of an extension point.
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {
        /**
         * To persist global configuration information,
         * simply store it in a field and call save().
         *
         * <p>
         * If you don't want fields to be persisted, use <tt>transient</tt>.
         */
        // private boolean useFrench;

        /**
         * In order to load the persisted global configuration, you have to
         * call load() in the constructor.
         */
        public DescriptorImpl() {
            load();
        }

        /**
         * Performs on-the-fly validation of the form field 'name'.
         * <p/>
         * //@param value This parameter receives the value that the user has typed.
         *
         * @return Indicates the outcome of the validation. This is sent to the browser.
         * <p/>
         * Note that returning {@link FormValidation#error(String)} does not
         * prevent the form from being saved. It just means that a message
         * will be displayed to the user.
         */
//        public FormValidation doCheckName(@QueryParameter String value)
//                throws IOException, ServletException {
//            if (value.length() == 0)
//                return FormValidation.error("Please set a name");
//            if (value.length() < 4)
//                return FormValidation.warning("Isn't the name too short?");
//            return FormValidation.ok();
//        }
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            // Indicates that this builder can be used with all kinds of project types 
            return true;
        }

        /**
         * This human readable name is used in the configuration screen.
         */
        public String getDisplayName() {
            return "Deploy to Mule";
        }


        /**
         * This method returns true if the global configuration says we should speak French.
         *
         * The method name is bit awkward because global.jelly calls this method to determine
         * the initial state of the checkbox by the naming convention.
         */
        //
    }
}

