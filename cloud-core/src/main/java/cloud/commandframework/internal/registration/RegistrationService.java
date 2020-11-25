package cloud.commandframework.internal.registration;

import cloud.commandframework.services.types.ConsumerService;

/**
 * A {@link cloud.commandframework.services.types.Service} that has the opportunity to alter
 * {@link cloud.commandframework.Command} parameters before the command is registered to the
 * {@link cloud.commandframework.CommandTree}
 *
 * @param <C> Command sender type
 */
public interface RegistrationService<C> extends ConsumerService<RegistrationService<C>> {
}
