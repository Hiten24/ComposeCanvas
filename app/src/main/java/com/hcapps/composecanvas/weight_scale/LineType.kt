package com.hcapps.composecanvas.weight_scale

sealed class LineType {
    object Normal: LineType()
    object FiveStep: LineType()
    object TenStep: LineType()
}