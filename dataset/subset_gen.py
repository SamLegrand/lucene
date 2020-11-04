from lxml import etree

context = etree.iterparse('./dataset/Posts.xml', events=('end',), tag='row')

i = 0
f = open('./dataset/subset.xml', 'wb')
f.write(b"<posts>\n")
for action, elem in context:
    i += 1
    new_row = etree.Element('row', attrib={attribute: elem.attrib[attribute] for attribute in ['Id', 'ParentId', 'AcceptedAnswerId', 'CreationDate', 'LastEditDate', 'Body', 'LastEditorDisplayName', 'Title', 'Tags'] if attribute in elem.attrib})
    f.write(etree.tostring(new_row) + b'\x0a')
    if i == 10000:
        break
f.write(b"</posts>")
f.close()