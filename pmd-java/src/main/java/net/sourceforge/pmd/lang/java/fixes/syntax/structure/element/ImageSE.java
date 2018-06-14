/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.fixes.syntax.structure.element;

import org.apache.commons.lang3.StringUtils;

import net.sourceforge.pmd.lang.ast.AbstractNode;
import net.sourceforge.pmd.lang.ast.GenericToken;
import net.sourceforge.pmd.lang.java.ast.JavaParserConstants;
import net.sourceforge.pmd.lang.java.fixes.factory.TokenFactory;
import net.sourceforge.pmd.lang.syntax.context.SyncContext;
import net.sourceforge.pmd.lang.syntax.parser.context.ParsePolicy;
import net.sourceforge.pmd.lang.syntax.parser.value.ParsedValue;
import net.sourceforge.pmd.lang.syntax.structure.TokenRegion;
import net.sourceforge.pmd.lang.syntax.structure.element.AbstractSingleMatchingStateSE;
import net.sourceforge.pmd.lang.syntax.structure.element.StructureElement;

public class ImageSE extends AbstractSingleMatchingStateSE {
    private static final String THIS_IMAGE = JavaTokenSE.THIS.getImage();

    public static ImageSE newInstance() {
        return new ImageSE();
    }

    @Override
    public boolean shouldMatch(final ParsePolicy policy, final ParsedValue value) {
        return policy == ParsePolicy.COMPLETE;
    }

    @Override
    public boolean doesMatch(final GenericToken parsedToken) {
        return parsedToken.is(JavaParserConstants.IDENTIFIER) || parsedToken.is(JavaParserConstants.THIS);
    }

    @Override
    public boolean doesMatch(final AbstractNode parsedChild) {
        return false;
    }

    @Override
    public TokenRegion sync(final SyncContext syncContext,
                            final StructureElement oldSE,
                            final TokenRegion oldTokenRegion) {
        final String nodeImage = syncContext.getNode().getImage();

        // First checking that we match oldSE so we can avoid stringifying a large token region unnecessarily.
        if (oldSE.equals(this) && oldTokenRegion.stringify().equals(nodeImage)) {
            return oldTokenRegion;
        }

        final String[] splitNodeImage = nodeImage.split("\\.");
        GenericToken newImageToken = null;
        if (splitNodeImage.length > 2) {
            invalidImageException();
        } else if (splitNodeImage.length == 2) {
            final String firstPart = splitNodeImage[0];
            final String secondPart = splitNodeImage[1];
            if (StringUtils.isNotBlank(firstPart) && THIS_IMAGE.equals(secondPart)) {
                newImageToken = TokenFactory.INSTANCE.newToken(JavaParserConstants.IDENTIFIER, nodeImage);
            } else {
                invalidImageException();
            }
        } else if (splitNodeImage.length == 1) {
            final String image = splitNodeImage[0];
            if (THIS_IMAGE.equals(image)) {
                newImageToken = TokenFactory.INSTANCE.newToken(JavaParserConstants.THIS, image);
            } else if (StringUtils.isBlank(image)) {
                invalidImageException();
            } else {
                newImageToken = TokenFactory.INSTANCE.newToken(JavaParserConstants.IDENTIFIER, image);
            }
        } else {
            invalidImageException();
        }

        return TokenRegion.newInstance(newImageToken, newImageToken); // If here, newImageToken should not be null.
    }

    private void invalidImageException() {
        throw new IllegalStateException("Invalid Image. Possibilities: <IDENTIFIER>.<THIS> || <THIS> || <IDENTIFIER>");
    }
}
