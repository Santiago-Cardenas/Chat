# Chat multiple client

## Integrantes:

- **Santiago Cárdenas Montes** - A00372312
- **Cristian Camilo Cardona** - A00369414

## Intrucciones para la ejecucion:

1. Navegar al directorio del código fuente:
    
    cd src

2. Compilar los archivos Java del servidor:

     javac server/*.java

3. Compilar los archivos Java del cliente:
    
    javac client/*.java

4.  Ejecutar primero el servidor (debe mantenerse corriendo): 
    
    java server.ChatServer

5. Ejecutar el cliente (en una o varias terminales para múltiples usuarios):

     java client.ChatClient

## Comandos disponibles en el Chat

 ```bash
- Enviar un mensaje privado: 

/private <usuario> <mensaje>

- Crear o unirse a un grupo y enviar mensajes:

/group <crear|unirse|mensaje> <nombreGrupo> [mensaje]

- Enviar un mensaje de voz:

/voice <usuario|grupo>

- Realizar una llamada:
/call <usuario|grupo>