package com.adnlv.lynd.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val MaterialSymbols.PlannerReview: ImageVector
    get() {
        if (_plannerReview != null) {
            return _plannerReview!!
        }
        _plannerReview = ImageVector.Builder(
            name = "planner_review",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                fillAlpha = 1f,
                stroke = null,
                strokeAlpha = 1f,
                strokeLineWidth = 1f,
                strokeLineCap = StrokeCap.Butt,
                strokeLineJoin = StrokeJoin.Bevel,
                strokeLineMiter = 1f,
                pathFillType = PathFillType.NonZero,
            ) {
                moveTo(12f, 22f)
                lineTo(9f, 7.95f)
                lineTo(6.8f, 18f)
                horizontalLineTo(2f)
                verticalLineTo(16f)
                horizontalLineTo(5.2f)
                lineTo(8.03f, 3f)
                horizontalLineTo(10f)
                lineToRelative(3.05f, 14.3f)
                lineTo(15f, 9f)
                horizontalLineToRelative(2f)
                lineToRelative(1.8f, 7f)
                horizontalLineTo(22f)
                verticalLineToRelative(2f)
                horizontalLineTo(17.25f)
                lineToRelative(-1.2f, -4.7f)
                lineTo(14f, 22f)
                horizontalLineTo(12f)
                close()
            }
        }.build()
        return _plannerReview!!
    }

private var _plannerReview: ImageVector? = null
