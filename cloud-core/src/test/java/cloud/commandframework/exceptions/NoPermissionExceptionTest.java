//
// MIT License
//
// Copyright (c) 2024 Incendo
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in all
// copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// SOFTWARE.
//
package cloud.commandframework.exceptions;

import cloud.commandframework.permission.Permission;
import cloud.commandframework.permission.PermissionResult;
import java.util.Collections;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SuppressWarnings("ThrowableNotThrown")
class NoPermissionExceptionTest {

    @Test
    void testSucceededResult() {
        PermissionResult result = PermissionResult.succeeded(Permission.of("konicai"));
        assertThrows(IllegalArgumentException.class, () -> new NoPermissionException(result, new Object(), Collections.emptyList()));
    }

    @Test
    void testCommandPermission() {
        Permission permission = Permission.of("konicai");
        PermissionResult result = PermissionResult.failed(permission);
        NoPermissionException exception = new NoPermissionException(result, new Object(), Collections.emptyList());

        assertEquals(permission, exception.missingPermission());
        assertEquals(permission, exception.permissionResult().permission());
    }
}
