from lxml import etree

context = etree.iterparse('./dataset/Posts.xml', events=('end',), tag='row')

f = open('./dataset/subset.xml', 'w')

i = 0
for action, elem in context:
    i += 1
    new_row = etree.Element('post', attrib={attribute: elem.attrib[attribute] for attribute in ['Id', 'ParentId', 'AcceptedAnswerId', 'CreationDate', 'LastEditDate', 'Body', 'LastEditorDisplayName', 'Title', 'Tags'] if attribute in elem.attrib})
    f.write(etree.tostring(new_row).decode('utf-8'))
    f.write('\n')
    if i == 10000:
        break
