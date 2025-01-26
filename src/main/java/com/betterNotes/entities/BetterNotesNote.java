package com.betterNotes.entities;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

public class BetterNotesNote
{
    @Getter
    @Setter
    private String id;

    @Getter @Setter
    private String name;

    @Getter @Setter
    private String content;

    public BetterNotesNote(final String name)
    {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.content = "Vivamus et eros sodales, feugiat nisl ut, sagittis nunc. Nam in venenatis nisi. Sed ultrices interdum sapien. Maecenas vestibulum neque id ultrices viverra. Pellentesque a euismod nisi. Aliquam consectetur metus nunc, in aliquam felis dignissim ut. Quisque et eros elit. Praesent ornare, leo nec blandit eleifend, urna nulla ornare magna, tempus condimentum augue nibh nec purus. Nulla venenatis et neque ac aliquet. Ut eu augue tincidunt, semper eros a, viverra tortor. Duis aliquet sapien dolor, non tempor nisl convallis ac. Donec at felis id eros tempor facilisis ac sit amet nulla.";
    }
}
