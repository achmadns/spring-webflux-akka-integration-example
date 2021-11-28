package id.co.alamisharia.simjam.config.support;

import akka.actor.*;

/**
 * NOTE: Still not sure how to create prototype bean with constructor parameter.
 */
public class SpringExtension extends AbstractExtensionId<SpringActorProvider> {

    public static final SpringExtension INSTANCE = new SpringExtension();

    @Override
    public SpringActorProvider createExtension(ExtendedActorSystem system) {
        return new SpringActorProvider();
    }

}
