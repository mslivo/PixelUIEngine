package net.mslivo.core.engine.ui_engine.rendering;

import com.badlogic.gdx.graphics.glutils.ShaderProgram;

public class OKLabShaders {

    private static final String PRIMITIVE_VERTEX_SHADER = """
            attribute vec4 $POSITION_ATTRIBUTE;
            attribute vec4 $COLOR_ATTRIBUTE;
            attribute vec4 $VERTEXCOLOR_ATTRIBUTE;
            attribute vec4 $TWEAK_ATTRIBUTE;
            
            uniform mat4 u_projTrans;
            
            varying vec4 fragColor;
            
            const vec3 forward = vec3(1.0 / 3.0);
            const float twoThird = 2.0 / 3.0;
            
            const mat3 rgbToLabMatrix = mat3(
                +0.2104542553, +1.9779984951, +0.0259040371,
                +0.7936177850, -2.4285922050, +0.7827717662,
                -0.0040720468, +0.4505937099, -0.8086757660
            );
            
            const mat3 rgbToXyzMatrix = mat3(
                0.4121656120, 0.2118591070, 0.0883097947,
                0.5362752080, 0.6807189584, 0.2818474174,
                0.0514575653, 0.1074065790, 0.6302613616
            );
            
            const mat3 labToRgbMatrix = mat3(
                1.0, 1.0, 1.0,
                +0.3963377774, -0.1055613458, -0.0894841775,
                +0.2158037573, -0.0638541728, -1.2914855480
            );
            
            const mat3 xyzToRgbMatrix = mat3(
                +4.0767245293, -1.2681437731, -0.0041119885,
                -3.3072168827, +2.6093323231, -0.7034763098,
                +0.2307590544, -0.3411344290, +1.7068625689
            );
            
            vec3 rgbToLabColor(vec3 color) {
                vec3 xyz = rgbToXyzMatrix * (color * color);
                vec3 lab = rgbToLabMatrix * pow(xyz, forward);
                lab.x = pow(lab.x, 1.48);
                lab.yz = lab.yz * 0.5 + 0.5;
                return lab;
            }
            
            vec3 rgbToLabFragment(vec3 color) {
                vec3 xyz = rgbToXyzMatrix * (color * color);
                vec3 lab = rgbToLabMatrix * pow(xyz, forward);
                lab.x = (pow(lab.x, 1.51) - 0.5) * 2.0;
                return lab;
            }
            
            void main() {
                gl_PointSize = 1.0;
            
                // Tint Color
                vec4 v_color = $COLOR_ATTRIBUTE;
                v_color.w *= 255.0 / 254.0;
                v_color.rgb = rgbToLabColor(v_color.rgb);
            
                // Position
            
                gl_Position = u_projTrans * $POSITION_ATTRIBUTE;
            
                // Draw
                vec3 tgtLab = rgbToLabFragment($VERTEXCOLOR_ATTRIBUTE.rgb);
                vec3 tweak = $TWEAK_ATTRIBUTE.rgb;
                vec3 color = v_color.rgb;
            
                tgtLab.x = pow(clamp(tgtLab.x * $TWEAK_ATTRIBUTE.x + color.x, 0.0, 1.0), twoThird);
                tgtLab.yz = clamp((tgtLab.yz * tweak.yz + color.yz - 0.5) * 2.0, -1.0, 1.0);
                vec3 lab = labToRgbMatrix * tgtLab;
            
                fragColor = vec4(sqrt(clamp(xyzToRgbMatrix * (lab * lab * lab), 0.0, 1.0)), v_color.a * $VERTEXCOLOR_ATTRIBUTE.a);
            }
            """;

    private static final String PRIMITIVE_FRAGMENT_SHADER = """
                #ifdef GL_ES
                    #define LOW lowp
                    #define MED mediump
                    #define HIGH highp
                    precision mediump float;
                #else
                    #define MED
                    #define LOW
                    #define HIGH
                #endif
            
                varying vec4 fragColor;
            
                void main() {
                   gl_FragColor = fragColor;
                }
            """;
}
