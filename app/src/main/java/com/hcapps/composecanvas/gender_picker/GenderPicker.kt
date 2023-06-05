package com.hcapps.composecanvas.gender_picker

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hcapps.Gender
import com.hcapps.composecanvas.R

@Composable
fun GenderPicker(
    modifier: Modifier = Modifier,
    maleGradient: List<Color> = listOf(Color(0xFF6D6DFF), Color.Blue),
    femaleGradient: List<Color> = listOf(Color(0xFFEA76FF), Color.Magenta),
    distanceBetweenGenders: Dp = 50.dp,
    pathScaleFactor: Float = 7f,
    onGenderSelected: (Gender) -> Unit
) {
    // remembers which gender is selected
    var selectedGender by remember {
        mutableStateOf<Gender>(Gender.Female)
    }
    // center of the canvas
    var center by remember {
        mutableStateOf(Offset.Unspecified)
    }

    // vector path of the male resources
    val malePathString = stringResource(id = R.string.male_path)
    // vector path of the female resources
    val femalePathString = stringResource(id = R.string.female_path)


    // male path parse from the vector resource path
    val malePath = remember { PathParser().parsePathString(malePathString).toPath() }
    // female path parse from the vector resource path
    val femalePath = remember { PathParser().parsePathString(femalePathString).toPath() }

    // bound of male resources i.e background rectangle which contains male vector
    val malePathBounds = remember { malePath.getBounds() }
    // bound of female resources i.e background rectangle which contains female vector
    val femalePathBounds = remember { femalePath.getBounds() }

    // since original vector size is very small and top left of the screen to transform to center
    // remembers center offset male vector
    var maleTranslationOffset by remember {
        mutableStateOf(Offset.Zero)
    }
    // remembers center offset of female vector
    var femaleTranslationOffset by remember {
        mutableStateOf(Offset.Zero)
    }

    // offset of clicked coordinate
    var currentClickOffset by remember {
        mutableStateOf(Offset.Zero)
    }

    // animate radius of the selected circle of male vector
    val maleSelectionRadius = animateFloatAsState(
        targetValue = if (selectedGender is Gender.Male)  80f else 0f,
        animationSpec = tween(durationMillis = 500)
    )

    // animate radius of the selected circle of female vector
    val feMaleSelectionRadius = animateFloatAsState(
        targetValue = if (selectedGender is Gender.Female)  80f else 0f,
        animationSpec = tween(durationMillis = 500)
    )

    Canvas(
        modifier = modifier
            .pointerInput(true) {
                detectTapGestures {
                    // to check tap is inside vector bound we have to transform male rect
                    val transformedMaleRect = Rect(
                        offset = maleTranslationOffset,
                        size = malePathBounds.size * pathScaleFactor
                    )
                    // to check tap is inside vector bound we have to transform female rect
                    val transformedFemaleRect = Rect(
                        offset = femaleTranslationOffset,
                        size = femalePathBounds.size * pathScaleFactor
                    )
                    // if male is not already selected and tap offset is inside transformed male rect then is selected male
                    if (selectedGender !is Gender.Male && transformedMaleRect.contains(it)) {
                        currentClickOffset = it
                        selectedGender = Gender.Male
                        onGenderSelected(Gender.Male)
                    }
                    // if female is not already selected and tap offset is inside transformed male rect then is selected female
                    else if (selectedGender !is Gender.Female && transformedFemaleRect.contains(it)) {
                        currentClickOffset = it
                        selectedGender = Gender.Female
                        onGenderSelected(Gender.Female)
                    }
                }
            }
    ) {
        // assigning center of the canvas
        center = this.center

        maleTranslationOffset = Offset(
            // scale width to 7 factor and reducing the half of the distance between gender
            x = center.x - malePathBounds.width * pathScaleFactor - distanceBetweenGenders.toPx() / 2f,
            // and reducing half of the bound height and scale to 7 factor
            y = center.y - pathScaleFactor * malePathBounds.height / 2f
        )
        femaleTranslationOffset = Offset(
            // since female vector already in right side since we are drawing from top left coordinates
            // so adding only half of the distance between genders
            x = center.x + distanceBetweenGenders.toPx() / 2f,
            // scaling 7 factor and with half of the height of the bound and reducing that from height
            y = center.y - pathScaleFactor * femalePathBounds.height / 2f
        )

        // keeping untransformed male to draw circle in right position else it will appear somewher else
        val untransformedMaleClickOffset = if (currentClickOffset == Offset.Zero) {
            malePathBounds.center
        } else {
            (currentClickOffset - maleTranslationOffset) / pathScaleFactor
        }

        // keeping untransformed male to draw circle in right position else it will appear somewher else
        val untransformedFemaleClickOffset = if (currentClickOffset == Offset.Zero) {
            femalePathBounds.center
        } else {
            (currentClickOffset - femaleTranslationOffset) / pathScaleFactor
        }

        // transform vector male to center
        translate(left = maleTranslationOffset.x, top = maleTranslationOffset.y) {
            // scaling vector to original size
            scale(scale = pathScaleFactor, pivot = malePathBounds.topLeft) {
                // draw actual path of male
                drawPath(path = malePath, color = Color.LightGray)
                // clip path to circle
                clipPath(path = malePath) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = maleGradient,
                            center = untransformedMaleClickOffset,
                            radius = maleSelectionRadius.value + 1
                        ),
                        center = untransformedMaleClickOffset,
                        radius = maleSelectionRadius.value
                    )
                }
            }
        }
        // same with female as above
        translate(left = femaleTranslationOffset.x, top = femaleTranslationOffset.y) {
            scale(scale = pathScaleFactor, pivot = femalePathBounds.topLeft) {
                drawPath(path = femalePath, color = Color.LightGray)
                clipPath(path = femalePath) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = femaleGradient,
                            center = untransformedFemaleClickOffset,
                            radius = feMaleSelectionRadius.value + 1
                        ),
                        center = untransformedFemaleClickOffset,
                        radius = feMaleSelectionRadius.value
                    )
                }
            }
        }
    }

}