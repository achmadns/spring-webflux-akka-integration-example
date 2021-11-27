package id.co.alamisharia.simjam.config;

import akka.actor.*;
import id.co.alamisharia.simjam.config.support.SpringActorProvider;

public class SpringExtension extends AbstractExtensionId<SpringActorProvider> {

    public static final SpringExtension INSTANCE = new SpringExtension();

    @Override
    public SpringActorProvider createExtension(ExtendedActorSystem system) {
        return new SpringActorProvider();
    }

}
