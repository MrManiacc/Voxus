#vertex
#version 400 core

in vec3 position;
in vec3 _normal;G
in vec2 _uv;

uniform mat4 viewMatrix;
uniform mat4 projectionMatrix;
uniform mat4 modelMatrix;
out vec3 normal;
out vec3 fragPos;
out vec2 uv;

void main(){
    gl_Position =  projectionMatrix * viewMatrix * modelMatrix * vec4(position, 1.0);
    normal = _normal;
    uv = _uv;
    fragPos = (modelMatrix * vec4(position, 1.0)).xyz;
}


#fragment
#version 400 core
#import BasicLight;


in vec3 normal;
in vec3 fragPos;
in vec2 uv;

out vec4 outColor;

uniform sampler2D diffuse;
void main(){
    vec2 coords = vec2(0.5,0.5);

    outColor = vec4(texture(diffuse, uv).rgb, 1.0);
}
