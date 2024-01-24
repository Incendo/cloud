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
package org.incendo.cloud.state;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class StatefulTest {

    private Stateful<TestState> stateful;

    @BeforeEach
    void setup() {
        this.stateful = new TestStateful();
    }

    @Test
    void RequireState_ExpectedState_Success() {
        // Act & Assert
        this.stateful.requireState(TestState.A);
    }

    @Test
    void RequireState_UnexpectedState_ThrowsException() {
        // Act & Assert
        assertThrows(
                IllegalStateException.class,
                () -> this.stateful.requireState(TestState.B)
        );
    }

    @Test
    void TransitionOrThrow_ExpectedInState_Success() {
        // Act
        this.stateful.transitionOrThrow(TestState.A, TestState.B);

        // Assert
        assertThat(this.stateful.state()).isEqualTo(TestState.B);
    }

    @Test
    void TransitionOrThrow_ExpectedOutState_Success() {
        // Act
        this.stateful.transitionOrThrow(TestState.C, TestState.A);

        // Assert
        assertThat(this.stateful.state()).isEqualTo(TestState.A);
    }

    @Test
    void TransitionOrThrow_UnexpectedState_ThrowsException() {
        // Act & Assert
        assertThrows(
                IllegalStateException.class,
                () -> this.stateful.transitionOrThrow(TestState.B, TestState.C)
        );
    }


    static final class TestStateful implements Stateful<TestState> {

        private TestState state = TestState.A;

        @Override
        public @NonNull TestState state() {
            return this.state;
        }

        @Override
        public boolean transitionIfPossible(final @NonNull TestState in, final @NonNull TestState out) {
            if (this.state == in) {
                this.state = out;
            }
            return this.state == out;
        }
    }

    enum TestState implements State {
        A,
        B,
        C
    }
}
