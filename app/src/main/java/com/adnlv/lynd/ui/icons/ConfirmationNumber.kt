package com.adnlv.lynd.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val MaterialSymbols.ConfirmationNumber: ImageVector
    get() {
        if (_confirmationNumber != null) {
            return _confirmationNumber!!
        }
        _confirmationNumber = ImageVector.Builder(
            name = "confirmation_number",
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
                moveTo(12f, 17f)
                quadToRelative(0.43f, 0f, 0.71f, -0.29f)
                quadTo(13f, 16.43f, 13f, 16f)
                reflectiveQuadTo(12.71f, 15.29f)
                reflectiveQuadTo(12f, 15f)
                reflectiveQuadToRelative(-0.71f, 0.29f)
                reflectiveQuadTo(11f, 16f)
                reflectiveQuadToRelative(0.29f, 0.71f)
                reflectiveQuadTo(12f, 17f)
                close()
                moveToRelative(0f, -4f)
                quadToRelative(0.43f, 0f, 0.71f, -0.29f)
                quadTo(13f, 12.43f, 13f, 12f)
                reflectiveQuadTo(12.71f, 11.29f)
                reflectiveQuadTo(12f, 11f)
                reflectiveQuadToRelative(-0.71f, 0.29f)
                reflectiveQuadTo(11f, 12f)
                reflectiveQuadToRelative(0.29f, 0.71f)
                reflectiveQuadTo(12f, 13f)
                close()
                moveTo(12f, 9f)
                quadToRelative(0.43f, 0f, 0.71f, -0.29f)
                reflectiveQuadTo(13f, 8f)
                quadTo(13f, 7.57f, 12.71f, 7.29f)
                reflectiveQuadTo(12f, 7f)
                reflectiveQuadTo(11.29f, 7.29f)
                reflectiveQuadTo(11f, 8f)
                quadToRelative(0f, 0.42f, 0.29f, 0.71f)
                reflectiveQuadTo(12f, 9f)
                close()
                moveToRelative(8f, 11f)
                horizontalLineTo(4f)
                quadTo(3.18f, 20f, 2.59f, 19.41f)
                reflectiveQuadTo(2f, 18f)
                verticalLineTo(14f)
                quadToRelative(0.83f, 0f, 1.41f, -0.59f)
                reflectiveQuadTo(4f, 12f)
                reflectiveQuadTo(3.41f, 10.59f)
                reflectiveQuadTo(2f, 10f)
                verticalLineTo(6f)
                quadTo(2f, 5.18f, 2.59f, 4.59f)
                reflectiveQuadTo(4f, 4f)
                horizontalLineTo(20f)
                quadToRelative(0.83f, 0f, 1.41f, 0.59f)
                quadTo(22f, 5.18f, 22f, 6f)
                verticalLineToRelative(4f)
                quadToRelative(-0.82f, 0f, -1.41f, 0.59f)
                quadTo(20f, 11.18f, 20f, 12f)
                reflectiveQuadToRelative(0.59f, 1.41f)
                reflectiveQuadTo(22f, 14f)
                verticalLineToRelative(4f)
                quadToRelative(0f, 0.82f, -0.59f, 1.41f)
                reflectiveQuadTo(20f, 20f)
                close()
                moveToRelative(0f, -2f)
                verticalLineTo(15.45f)
                quadTo(19.08f, 14.9f, 18.54f, 13.99f)
                quadTo(18f, 13.08f, 18f, 12f)
                reflectiveQuadToRelative(0.54f, -1.99f)
                reflectiveQuadTo(20f, 8.55f)
                verticalLineTo(6f)
                horizontalLineTo(4f)
                verticalLineTo(8.55f)
                quadTo(4.93f, 9.1f, 5.46f, 10.01f)
                reflectiveQuadTo(6f, 12f)
                reflectiveQuadTo(5.46f, 13.99f)
                reflectiveQuadTo(4f, 15.45f)
                verticalLineTo(18f)
                horizontalLineTo(20f)
                close()
                moveTo(12f, 12f)
                close()
            }
        }.build()
        return _confirmationNumber!!
    }

private var _confirmationNumber: ImageVector? = null

val MaterialSymbols.Ticket: ImageVector
    get() = ConfirmationNumber
