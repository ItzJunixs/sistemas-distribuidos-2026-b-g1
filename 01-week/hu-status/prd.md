# Product Brief

project_key: PRJ-FERRETERIA-V13

## Declared Tech Stack

- backend: Go
- database: MySQL
- frontend: Angular

## Human Entrada Context

## 00-contexto-inicial.md

# Contexto inicial

La ferreteria es un negocio pequeno que opera todos los dias y mueve dinero por ingresos y egresos. El dueno o administrador necesita una forma simple de saber cuanto dinero entro, cuanto salio y cual es el saldo neto del negocio por periodo.

El sistema buscado no es un punto de venta ni un sistema para registrar cada venta individual. La necesidad principal es control financiero agregado: registrar movimientos de dinero por concepto y consultar resúmenes diarios, semanales y mensuales.

Preferencia tecnologica del producto para esta prueba: Angular para la interfaz, Go para backend y MySQL para base de datos.

## 01-necesidades-y-problemas.md

# Necesidades y problemas

- Registrar ingresos de dinero de forma simple, por ejemplo ventas agregadas del dia, abonos u otros ingresos.
- Registrar egresos de dinero, por ejemplo compras de mercancia, pagos a proveedores, gastos operativos, servicios o transporte.
- Consultar cuanto entro, cuanto salio y saldo neto en un dia, semana o mes.
- Diferenciar categorias de ingresos y gastos para entender de donde viene o hacia donde sale el dinero.
- Evitar depender de papel, memoria, mensajes o cuentas sueltas.
- Mantener la operacion simple para un negocio pequeno; no se quiere un ERP, facturacion completa ni POS.

Problema principal: el administrador no tiene visibilidad inmediata del estado financiero y solo puede reconstruirlo manualmente.

## 02-procesos-actuales.md

# Procesos actuales

Actualmente el negocio puede anotar movimientos en papel, mensajes o recordarlos de memoria. Al final del dia o cuando necesita revisar dinero, el administrador suma manualmente ingresos y egresos.

Proceso esperado:

1. Al recibir dinero, registrar un ingreso con fecha, categoria, valor y nota opcional.
2. Al pagar o gastar dinero, registrar un egreso con fecha, categoria, valor y nota opcional.
3. Consultar un resumen por periodo: dia, semana o mes.
4. Ver totales de ingresos, totales de egresos y saldo neto.
5. Revisar detalle de movimientos cuando el saldo no cuadre.

No se requiere inventario detallado ni registrar cada producto vendido.

## 03-preguntas-abiertas.md

# Preguntas abiertas

- Que categorias iniciales de ingreso y egreso debe traer el sistema?
- Habra un solo usuario administrador o varios usuarios?
- Se necesita cierre o corte diario con bloqueo de edicion posterior?
- Se deben poder editar o anular movimientos ya registrados?
- Se requiere exportar reportes a CSV o PDF?
- El saldo neto se calcula solo por periodo o tambien debe mostrar saldo acumulado?
- Se necesita adjuntar comprobantes o fotos de recibos en una fase futura?
- Que nivel de seguridad se espera para acceso al sistema?

## 04-glosario-negocio.md

# Glosario de negocio

- Ingreso: dinero que entra al negocio por ventas agregadas, abonos u otros conceptos.
- Egreso: dinero que sale del negocio por compras, gastos, pagos o servicios.
- Saldo neto: ingresos menos egresos en un periodo.
- Corte diario: resumen del movimiento financiero de un dia.
- Gasto operativo: salida de dinero necesaria para operar, como servicios, transporte o mantenimiento.
- Compra de mercancia: salida de dinero para adquirir productos que luego se venderan en la ferreteria.
- Periodo: rango de consulta, por ejemplo dia, semana o mes.
- Categoria: clasificacion del movimiento para entender su origen o destino.

