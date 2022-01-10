package com.github.mygreen.splate;

import java.util.Set;


/**
 * 名前付きパラメータを解決します。
 *
 * @since 0.3
 * @author T.TSUCHIE
 *
 */
public class NamedParamBindNameResolver {

    /**
     * 新しいバインド変数名を作成します。
     * @param usedNames 使用済みの名称
     * @param bindName これから割り振りたいバインド変数の候補
     * @return 新しいバインド変数名。
     */
    public String newBindName(final Set<String> usedNames, final String bindName) {
        if(!usedNames.contains(bindName)) {
            return bindName;
        }

        String newName = bindName;
        for(int i=1; usedNames.contains(newName); i++) {
            newName = bindName + "_" + i;
        }

        return newName;
    }
}
