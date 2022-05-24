/**
 * Brigadier mappings.
 *
 * <p>For platform implementations using Brigadier, {@link cloud.commandframework.brigadier.CloudBrigadierManager} can map
 * Cloud {@link cloud.commandframework.CommandTree command trees} to Brigadier nodes.</p>
 *
 * <p>To bridge Brigadier and Cloud argument types, an argument parser that wraps Brigadier argument types is available in
 * {@link cloud.commandframework.brigadier.argument.WrappedBrigadierParser}. Other classes in that package allow constructing
 * Brigadier {@link com.mojang.brigadier.StringReader} instances that can be used for efficient interoperability with
 * Brigadier.</p>
 */
package cloud.commandframework.brigadier;
