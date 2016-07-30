package com.icrowsoft.blackspotter.roundImage;

public class CreateMyRoundedDrawable {

    public static TextDrawable CreateRoundedImage(String initials) {

        // draw round image and randomize colors
        ColorGenerator generator = ColorGenerator.MATERIAL; // or use DEFAULT

        TextDrawable drawable = TextDrawable.builder().buildRound(initials, generator.getRandomColor());
        return drawable;
    }
}
