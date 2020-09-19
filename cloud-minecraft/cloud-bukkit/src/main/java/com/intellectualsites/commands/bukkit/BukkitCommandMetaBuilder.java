//
// MIT License
//
// Copyright (c) 2020 Alexander Söderberg
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
package com.intellectualsites.commands.bukkit;

import com.intellectualsites.commands.meta.CommandMeta;

import javax.annotation.Nonnull;

public final class BukkitCommandMetaBuilder {

    private BukkitCommandMetaBuilder() {
    }

    /**
     * Create a new builder stage 1
     *
     * @return Builder instance
     */
    @Nonnull
    public static BuilderStage1 builder() {
        return new BuilderStage1();
    }


    public static final class BuilderStage1 {

        private BuilderStage1() {
        }

        /**
         * Set the command description
         *
         * @param description Command description
         * @return Builder instance
         */
        @Nonnull
        public BuilderStage2 withDescription(@Nonnull final String description) {
            return new BuilderStage2(description);
        }

    }


    public static final class BuilderStage2 {

        private final String description;

        private BuilderStage2(@Nonnull final String description) {
            this.description = description;
        }

        /**
         * Build the command meta instance
         *
         * @return Meta instance
         */
        @Nonnull
        public BukkitCommandMeta build() {
            return new BukkitCommandMeta(CommandMeta.simple().with("description", this.description).build());
        }

    }

}
