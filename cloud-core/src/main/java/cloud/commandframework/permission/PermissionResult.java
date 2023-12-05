package cloud.commandframework.permission;

public interface PermissionResult {

    PermissionResult TRUE = () -> true;
    PermissionResult FALSE = () -> false;

    boolean toBoolean();

    static PermissionResult of(boolean result) {
        return result ? TRUE : FALSE;
    }
}
