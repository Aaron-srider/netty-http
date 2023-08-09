package com.example.base

import io.netty.buffer.Unpooled
import java.lang.reflect.Field
import java.util.*
import cn.hutool.core.util.ReflectUtil


class Utils {
}

/**
 * @author ccm
 * @version V1.0
 * @package com.ccm.base.util
 * @date 2022/8/6 11:10
 * @copyright © 2020-2021 ccm
 */
interface ModelSerialization {
    fun toBytes(): ByteArray {
        val byteBuf = Unpooled.buffer()
        val fields: Array<Field> = ReflectUtil.getFields(this.javaClass)
        Arrays.stream<Field>(fields).forEach { field: Field ->
            val value: Any = ReflectUtil.getFieldValue(this, field)
            val fn = field.type.simpleName
            when (fn) {
                "byte" -> byteBuf.writeByte(value.toString().toByte().toInt())
                "short" -> byteBuf.writeShort(value.toString().toShort().toInt())
                "int" -> byteBuf.writeInt(value.toString().toInt())
                "long" -> byteBuf.writeLong(value.toString().toLong())
                "byte[]" -> if (value != null) {
                    byteBuf.writeBytes(value as ByteArray)
                }
                else -> {}
            }
        }
        return Arrays.copyOf(byteBuf.array(), byteBuf.writerIndex())
    }

    fun fromBytes(data: ByteArray) {
        val clazz: Class<out ModelSerialization> = this.javaClass
        var obj: ModelSerialization? = null
        obj = try {
            clazz.newInstance()
        } catch (e: InstantiationException) {
            throw RuntimeException(e)
        } catch (e: IllegalAccessException) {
            throw RuntimeException(e)
        }
        val byteBuf = Unpooled.wrappedBuffer(data)
        val fields: Array<Field> = ReflectUtil.getFields(clazz)
        Arrays.stream<Field>(fields).forEach { field: Field ->
            val fn = field.type.simpleName
            when (fn) {
                "byte" -> ReflectUtil.setFieldValue(this, field, byteBuf.readByte())
                "short" -> ReflectUtil.setFieldValue(this, field, byteBuf.readShort())
                "int" -> ReflectUtil.setFieldValue(this, field, byteBuf.readInt())
                "long" -> ReflectUtil.setFieldValue(this, field, byteBuf.readLong())
                "byte[]" -> {
                    val tmp: ByteArray
                    val length: Int
                    length = if (ReflectUtil.hasField(clazz, field.name + LENGTH)) {
                        ReflectUtil.getFieldValue(
                            this,
                            field.name + LENGTH
                        ) as Int
                    } else if (ReflectUtil.getFieldValue(this, field) != null) {
                        (ReflectUtil.getFieldValue(this, field) as ByteArray).size
                    } else {
                        byteBuf.readableBytes()
                    }
                    tmp = ByteArray(length)
                    byteBuf.readBytes(tmp)
                    ReflectUtil.setFieldValue(this, field, tmp)
                }

                else -> {}
            }
        }
    }

    companion object {
        const val LENGTH = "Length"
        val HEX_CHARS = charArrayOf(
            '0', '1', '2', '3', '4', '5',
            '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
        )
    }
}
