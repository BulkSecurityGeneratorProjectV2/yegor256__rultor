/**
 * Copyright (c) 2009-2021 Yegor Bugayenko
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met: 1) Redistributions of source code must retain the above
 * copyright notice, this list of conditions and the following
 * disclaimer. 2) Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided
 * with the distribution. 3) Neither the name of the rultor.com nor
 * the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.rultor.web;

import com.jcabi.matchers.XhtmlMatchers;
import com.rultor.spi.Talk;
import com.rultor.spi.Talks;
import org.hamcrest.MatcherAssert;
import org.junit.Test;
import org.takes.facets.fork.RqRegex;
import org.takes.facets.fork.TkRegex;
import org.takes.rq.RqFake;
import org.takes.rq.RqWithHeader;
import org.takes.rs.RsPrint;
import org.xembly.Directives;

/**
 * Test case for {@link TkSiblings}.
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 1.23.1
 * @checkstyle MultipleStringLiteralsCheck (500 lines)
 */
public final class TkSiblingsTest {

    /**
     * TkSiblings can render a list.
     * @throws Exception If some problem inside
     */
    @Test
    public void rendersListOfTalks() throws Exception {
        final Talks talks = new Talks.InDir();
        final TkRegex take = new TkSiblings(talks);
        talks.create("repo1", Talk.TEST_NAME);
        talks.get(Talk.TEST_NAME).modify(
            new Directives()
                .xpath("/talk")
                .push().xpath("wire").remove().pop()
                .add("wire").add("href").set("http://example.com").up()
                .add("github-repo").set("yegor256/rultor").up()
                .add("github-issue").set("555").up().up()
                .add("archive").add("log").attr("title", "hello, world")
                .attr("id", "a1b2c3").set("s3://test")
        );
        MatcherAssert.assertThat(
            XhtmlMatchers.xhtml(
                new RsPrint(
                    take.act(
                        new RqRegex.Fake(
                            new RqWithHeader(
                                new RqFake("GET", "/aa?s=123"),
                                "Accept", "text/xml"
                            ),
                            "(.*)",
                            "x"
                        )
                    )
                ).printBody()
            ),
            XhtmlMatchers.hasXPaths(
                "/page[repo='x']",
                "/page[since='123']",
                "/page/siblings[count(talk)=1]",
                "/page/siblings/talk[timeago]",
                "/page/siblings/talk/archive/log[id and href and title]",
                "/page/siblings/talk/archive[count(log)=1]",
                "//log[starts-with(href,'https://')]"
            )
        );
    }

}
