-- =========================================
-- MIGRACIÓN: Agregar estados 'cancelada' y 'cancelado'
-- =========================================
-- Este script agrega los valores faltantes a los ENUMs de PostgreSQL
-- para permitir la cancelación de compras online y pagos.
--
-- ERROR CORREGIDO:
-- ERROR: new row for relation "compras_online" violates check constraint "compras_online_estado_compra_check"
-- Detail: Failing row contains (..., cancelada, ...)
--
-- CAUSA: El ENUM estado_compra_enum no incluía 'cancelada' 
--        El ENUM estado_pago_enum no incluía 'cancelado'
--
-- SOLUCIÓN: Agregar los valores faltantes usando ALTER TYPE
-- =========================================

-- 1. Agregar 'cancelada' al enum estado_compra_enum
ALTER TYPE estado_compra_enum ADD VALUE IF NOT EXISTS 'cancelada';

-- 2. Agregar 'cancelado' al enum estado_pago_enum  
ALTER TYPE estado_pago_enum ADD VALUE IF NOT EXISTS 'cancelado';

-- =========================================
-- VERIFICACIÓN
-- =========================================
-- Para verificar que los valores fueron agregados correctamente:
-- 
-- SELECT enum_range(NULL::estado_compra_enum);
-- Resultado esperado: {pendiente,procesada,enviada,entregada,cancelada}
--
-- SELECT enum_range(NULL::estado_pago_enum);
-- Resultado esperado: {pendiente,completado,fallido,cancelado}
-- =========================================
