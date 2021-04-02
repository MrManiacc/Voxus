#vertex
#version 400 core
//Testing

in vec3 position;
in vec3 _normal;
in vec3 _color;

uniform mat4 viewMatrix;
uniform mat4 projectionMatrix;
uniform mat4 modelMatrix;
out vec3 normal;
out vec3 fragPos;
out vec3 color;

void main(){
    gl_Position =  projectionMatrix * viewMatrix * modelMatrix * vec4(position, 1.0);
    normal = _normal;
    fragPos = (modelMatrix * vec4(position, 1.0)).xyz;
    color = _color;
}


#fragment
#version 400 core
#import BasicMaterial;
#import BasicLight;
#import Camera;

in vec3 normal;
in vec3 color;
in vec3 fragPos;

uniform Light light;
uniform Material material;

out vec4 outColor;

void main(){

    // ambient
    float ambientStrength = 0.1;
    vec3 ambient = ambientStrength * light.color;

    // diffuse
    vec3 norm = normalize(normal);
    vec3 lightDir = normalize(light.pos - fragPos);
    float diff = max(dot(norm, lightDir), 0.0);
    vec3 diffuse = diff * light.color;

    // specular
    float specularStrength = 0.1;
    vec3 viewDir = normalize(camera - fragPos);
    vec3 reflectDir = reflect(-lightDir, norm);
    float spec = pow(max(dot(viewDir, reflectDir), 0.0), 16);
    vec3 specular = specularStrength * spec * light.color;

    vec3 result = (ambient + diffuse + specular) * color;
//    vec3 result = (ambient + diffuse + specular) * material.color.xyz;

    outColor = vec4(result, 1.0);
}
