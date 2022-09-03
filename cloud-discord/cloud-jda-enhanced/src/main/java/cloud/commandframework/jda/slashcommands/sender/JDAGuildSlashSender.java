//
// MIT License
//
// Copyright (c) 2022 Alexander Söderberg & Contributors
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

package cloud.commandframework.jda.slashcommands.sender;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import org.checkerframework.checker.nullness.qual.NonNull;

public class JDAGuildSlashSender extends JDASlashCommandSender implements JDAGuildSender {

    @NonNull
    private final Member member;
    @NonNull
    private final TextChannel textChannel;

    public JDAGuildSlashSender(
            final @NonNull SlashCommandEvent event,
            final @NonNull Member member,
            final @NonNull TextChannel textChannel
    ) {
        super(event, member.getUser(), textChannel);
        this.textChannel = textChannel;
        this.member = member;
    }

    @Override
    public @NonNull Member getMember() {
        return member;
    }

    @Override
    public @NonNull TextChannel getTextChannel() {
        return textChannel;
    }

}
