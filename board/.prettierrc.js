module.exports = {
  "plugins": ["@trivago/prettier-plugin-sort-imports"],
  "importOrder": [
    "^node:",                     
    "<THIRD_PARTY_MODULES>",      
    "^[./]",                      
    "^@common/(.*)$",            
    "^@domain/(.*)$",             
    "^@dto/(.*)$" ,
    "^@enum/(.*)$" ,
    "^@decorator/(.*)$" ,
    "^@generated/(.*)$" ,
  ],
  "importOrderSeparation": true,  // 그룹 사이에 빈 줄 추가
  "importOrderSortSpecifiers": true, // 중괄호 {} 안의 요소들도 알파벳 순 정렬
  "importOrderParserPlugins": ["typescript", "decorators-legacy"], // 데코레이터 못읽는 버그 해결용
  "printWidth": 140
}