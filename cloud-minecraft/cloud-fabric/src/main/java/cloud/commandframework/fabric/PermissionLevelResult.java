package cloud.commandframework.fabric;

import cloud.commandframework.permission.PermissionResult;

public class PermissionLevelResult implements PermissionResult {

    private final boolean result;
    private final int requiredPermissionLevel;

    public PermissionLevelResult(final boolean result, final int requiredPermissionLevel) {
        this.result = result;
        this.requiredPermissionLevel = requiredPermissionLevel;
    }

    @Override
    public boolean toBoolean() {
        return result;
    }

    public int requiredPermissionLevel() {
        return requiredPermissionLevel;
    }
}
