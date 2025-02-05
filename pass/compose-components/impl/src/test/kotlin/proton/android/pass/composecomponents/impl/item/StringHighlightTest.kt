/*
 * Copyright (c) 2023 Proton AG
 * This file is part of Proton AG and Proton Pass.
 *
 * Proton Pass is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Proton Pass is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Proton Pass.  If not, see <https://www.gnu.org/licenses/>.
 */

package proton.android.pass.composecomponents.impl.item

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import kotlin.test.assertEquals

class StringHighlightTest {

    @Test
    fun `can highlight an empty string`() {
        assertThat("".highlight(emptySequence())).isEqualTo(AnnotatedString(""))
    }

    @Test
    fun `can highlight a word in the middle of the string`() {
        val res = process("one two three", "two")
        assertThat(res).isEqualTo(
            buildAnnotatedString {
                append("one ")
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                    append("two")
                }
                append(" three")
            }
        )
    }

    @Test
    fun `can highlight a word at the start of the string`() {
        val res = process("one two three four", "one")
        assertThat(res).isEqualTo(
            buildAnnotatedString {
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                    append("one")
                }
                append(" two thre…")
            }
        )
    }

    @Test
    fun `can highlight a word at the end of the string`() {
        val res = process("one two three four", "four")
        assertThat(res).isEqualTo(
            buildAnnotatedString {
                append("…two three ")
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                    append("four")
                }
            }
        )
    }

    @Test
    fun `can highlight a word that is not in the string`() {
        val res = process("one two three four", "five")
        assertThat(res).isEqualTo(
            buildAnnotatedString {
                append("one two three four")
            }
        )
    }

    @Suppress("MaxLineLength")
    @Test
    fun `can highlight a word in the middle of a long sentence string`() {
        val input = "Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Aenean commodo ligula eget dolor. Aenean massa. Cum sociis natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Donec quam felis, ultricies nec, pellentesque eu, pretium quis, sem. Nulla consequat massa quis enim. Donec pede justo, fringillum vel, aliquet nec, vulputate eget, arcu. In enim justo, rhoncus ut, imperdiet a, venenatis vitae, justo. Nullam dictum felis eu pede mollis pretium. Integer tincidunt. Cras dapibus. Vivamus elementum semper nisi. Aenean vulputate eleifend tellus. Aenean leo ligula, porttitor eu, consequat vitae, eleifend ac, enim. Aliquam lorem ante, dapibus in, viverra quis, feugiat a, tellus. Phasellus viverra nulla ut metus varius laoreet. Quisque rutrum. Aenean imperdiet. Etiam ultricies nisi vel augue. Curabitur ullamcorper ultricies nisi. Nam eget dui. Etiam rhoncus. Maecenas tempus, tellus eget condimentum rhoncus, sem quam semper libero, sit amet adipiscing sem neque sed ipsum. Nam quam nunc, blandit vel, luctus pulvinar, hendrerit id, lorem. Maecenas nec odio et ante tincidunt tempus. Donec vitae sapien ut libero venenatis faucibus. Nullam quis ante. Etiam sit amet orci eget eros faucibus tincidunt. Duis leo. Sed fringilla mauris sit amet nibh. Donec sodales sagittis magna."
        val res = process(input, "fringilla")
        val expected = buildAnnotatedString {
            append("… leo. Sed ")
            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                append("fringilla")
            }
            append(" mauris s…")
        }
        assertEquals(expected, res)
    }

    @Suppress("MaxLineLength")
    @Test
    fun `can highlight a word that appears multiple times, only reporting first two instances`() {
        val input = "En un lugar de la Mancha, de cuyo nombre no quiero acordarme, no ha mucho tiempo que vivía un hidalgo de los de lanza en astillero, adarga antigua, rocín flaco y galgo corredor. Una olla de algo más vaca que carnero, salpicón las más noches, duelos y quebrantos los sábados, lantejas los viernes, algún palomino de añadidura los domingos, consumían las tres partes de su hacienda. El resto della concluían sayo de velarte, calzas de velludo para las fiestas, con sus pantuflos de lo mesmo, y los días de entresemana se honraba con su vellorí de lo más fino. Tenía en su casa una ama que pasaba de los cuarenta, y una sobrina que no llegaba a los veinte, y un mozo de campo y plaza, que así ensillaba el rocín como tomaba la podadera. Frisaba la edad de nuestro hidalgo con los cincuenta años; era de complexión recia, seco de carnes, enjuto de rostro, gran madrugador y amigo de la caza. Quieren decir que tenía el sobrenombre de Quijada, o Quesada, que en esto hay alguna diferencia en los autores que deste caso escriben; aunque, por conjeturas verosímiles, se deja entender que se llamaba Quejana. Pero esto importa poco a nuestro cuento; basta que en la narración dél no se salga un punto de la verdad."
        val res = process(input, "rocín")
        val expected = buildAnnotatedString {
            // First part
            append("… antigua, ")
            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                append("rocín")
            }
            append(" flaco y …")
        }
        assertEquals(expected, res)
    }

    private fun process(input: String, highlight: String): AnnotatedString {
        val matches = highlight.toRegex().findAll(input)
        return input.highlight(matches)
    }

}
