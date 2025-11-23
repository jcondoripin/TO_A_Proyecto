# Guía de Personalización de Imágenes - Battle Simulator

## Sistema de Imágenes Mejorado

El juego ahora incluye un **sistema de imágenes personalizable** para representar a los guerreros en el campo de batalla. Cada tipo de guerrero tiene su propia imagen que se colorea según el clan.

## Estructura de Carpetas

Para agregar imágenes personalizadas, crea esta estructura:

```
TO_A_Proyecto/
├── resources/
│   └── images/
│       └── warriors/
│           ├── melee.png      (Guerrero Cuerpo a Cuerpo)
│           ├── ranged.png     (Guerrero a Distancia)
│           ├── magic.png      (Guerrero Mágico)
│           └── default.png    (Imagen por defecto)
├── src/
└── pom.xml
```

## Especificaciones de Imágenes

### Formato y Tamaño
- **Formato**: PNG con transparencia (recomendado)
- **Tamaño recomendado**: 48x48 píxeles o 64x64 píxeles
- **Fondo**: Transparente (alpha channel)
- **Estilo**: Iconos claros y reconocibles

### Tipos de Guerreros

| Archivo | Tipo | Descripción |
|---------|------|-------------|
| `melee.png` | Cuerpo a Cuerpo | Guerrero con espada/hacha |
| `ranged.png` | A Distancia | Arquero/Ballesta |
| `magic.png` | Mágico | Mago/Hechicero |
| `default.png` | Por Defecto | Usado si falta alguna imagen |

## Cómo Funcionan los Colores

1. **Color Base**: Las imágenes deben ser mayormente neutras (grises, blancos, negros)
2. **Tinte Automático**: El sistema aplica un tinte del color del clan sobre la imagen
3. **Visibilidad**: Los colores del clan se mezclan con la imagen al 40% de opacidad

### Ejemplo de Flujo:
```
Imagen Base (gris) + Color del Clan (rojo) = Guerrero Rojo
Imagen Base (gris) + Color del Clan (azul) = Guerrero Azul
```

## Imágenes Por Defecto

Si no agregas imágenes personalizadas, el sistema **genera automáticamente** iconos con letras:
- **M** = Melee (Rojo)
- **R** = Ranged (Verde)
- **W** = Magic/Wizard (Azul)

## Cómo Agregar Imágenes Personalizadas

### Paso 1: Crear la Carpeta
```bash
mkdir -p resources/images/warriors
```

### Paso 2: Agregar tus Imágenes
Coloca tus archivos PNG en `resources/images/warriors/`:
- `melee.png`
- `ranged.png`
- `magic.png`

### Paso 3: Reiniciar el Juego
El sistema carga las imágenes al iniciar. Si las imágenes existen, las usará; si no, usará las generadas automáticamente.

## Recomendaciones de Diseño

### Buenos Diseños
- Iconos simples y claros
- Siluetas reconocibles
- Contornos definidos
- Colores neutros (gris, blanco, negro)
- Fondo transparente

### Evitar
- Imágenes muy detalladas (se pierden a tamaño pequeño)
- Colores muy saturados (interfieren con el tinte del clan)
- Fondos opacos
- Imágenes muy grandes (>128x128)

## Herramientas Recomendadas

### Para Crear/Editar
- **GIMP** (gratuito, multiplataforma)
- **Krita** (gratuito, ideal para arte digital)
- **Photoshop** (comercial)
- **Paint.NET** (gratuito, Windows)
- **Pixlr** (online, gratuito)

### Para Encontrar Iconos
- **Game-icons.net** - Miles de iconos de juegos gratis
- **Flaticon** - Iconos vectoriales
- **Noun Project** - Iconos simples
- **Itch.io** - Packs de assets de juegos

## Ejemplos de Búsqueda

Para encontrar iconos apropiados, busca:
- "warrior icon"
- "knight silhouette"
- "archer symbol"
- "wizard icon"
- "medieval warrior"
- "rpg class icons"

## Recargar Imágenes

Si cambias las imágenes mientras el juego está ejecutándose, necesitarás:
1. Cerrar el juego
2. Reemplazar las imágenes
3. Reiniciar el juego

## Vista en el Juego

Las imágenes se muestran:
- En el **tablero de batalla** (12x12 grid)
- Con el **ID del guerrero** debajo
- Con la **salud (HP)** visible
- **Coloreadas** según el clan
- Con **tooltip** detallado al pasar el mouse

## Información del Tooltip

Al pasar el mouse sobre un guerrero verás:
```
[Nombre del Guerrero]
Tipo: MELEE/RANGED/MAGIC
Salud: 100
Escudo: 50
Fuerza: 75
Arma: Espada de Fuego
```

## Paleta de Colores del Juego

El tema visual usa:
- **Fondo Oscuro**: #14141E (20, 20, 30)
- **Secundario**: #1E1E2D (30, 30, 45)
- **Oro**: #DAA520 (218, 165, 32)
- **Texto Claro**: #F0F0F0 (240, 240, 240)
- **Grid Vacío**: #282832 (40, 40, 50)

## Dimensiones en Pantalla

| Contexto | Tamaño |
|----------|--------|
| Botón del Grid | 70x70 px |
| Icono del Guerrero | 48x48 px |
| Espacio para Texto | 22 px debajo del icono |

## Solución de Problemas

### Las imágenes no se cargan
1. Verifica que la carpeta `resources/images/warriors/` existe
2. Confirma que los archivos se llaman exactamente: `melee.png`, `ranged.png`, `magic.png`
3. Asegúrate de que los archivos son PNG válidos
4. Revisa la consola para mensajes de error

### Los colores se ven mal
- Las imágenes deben ser neutrales (grises)
- Evita colores muy saturados en la imagen base
- El sistema aplica un tinte, no reemplaza colores

### Las imágenes se ven borrosas
- Usa imágenes de al menos 48x48 px
- Evita redimensionar desde tamaños muy grandes
- Guarda en PNG sin compresión excesiva

## Ejemplo: Crear un Icono Personalizado

### En GIMP:
1. Crear nueva imagen: 64x64 px
2. Agregar capa de transparencia
3. Dibujar silueta del guerrero en gris/blanco
4. Agregar contorno negro
5. Exportar como PNG con transparencia
6. Guardar en `resources/images/warriors/melee.png`

### Estilo Recomendado:
```
- Silueta sólida en gris claro (#C0C0C0)
- Contorno negro (#000000) de 2-3 px
- Fondo totalmente transparente
- Detalles mínimos pero reconocibles
```




