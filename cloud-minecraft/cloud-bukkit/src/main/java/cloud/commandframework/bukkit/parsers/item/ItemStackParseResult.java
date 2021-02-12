//
// MIT License
//
// Copyright (c) 2021 Alexander Söderberg & Contributors
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
package cloud.commandframework.bukkit.parsers.item;

import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Optional;

class ItemStackParseResult {

    static ItemStackParseResult success(@NonNull ItemStack item) {
        return new ItemStackParseResult(item);
    }

    static ItemStackParseResult failure(ItemStackArgument.ItemStackParseException exception) {
        return new ItemStackParseResult(exception);
    }

    private final Optional<ItemStack> result;
    private ItemStackArgument.ItemStackParseException exception;

    private ItemStackParseResult(@NonNull ItemStack result) {
        this.result = Optional.of(result);
    }

    private ItemStackParseResult(ItemStackArgument.ItemStackParseException exception) {
        this.exception = exception;
        this.result = Optional.empty();
    }

    @NonNull
    public Optional<ItemStack> getResult() {
        return result;
    }

    public ItemStackArgument.ItemStackParseException getException() {
        return exception;
    }

}
