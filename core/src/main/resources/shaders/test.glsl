#vertex
#version 400 core
//Testing

in vec3 position;
in vec3 _normal;
in vec2 _uv;

uniform mat4 viewMatrix;
uniform mat4 projectionMatrix;
uniform mat4 modelMatrix;
out vec3 normal;
out vec3 fragPos;

void main(){
    gl_Position =  projectionMatrix * viewMatrix * modelMatrix * vec4(position, 1.0);
    normal = _normal;
    fragPos = (modelMatrix * vec4(position, 1.0)).xyz;
}


#fragment
#version 400 core

in vec3 normal;
in vec3 fragPos;

out vec4 outColor;

void main(){

    outColor = vec4(normal, 1.0);
}
