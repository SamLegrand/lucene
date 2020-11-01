from lxml import etree

context = etree.iterparse('./dataset/Posts.xml', events=('end',), tag='row')

f = open('./dataset/subset.xml', 'wb')

i = 0
# lines = []
my_bytes = bytearray()
for action, elem in context:
    i += 1
    new_row = etree.Element('post', attrib={attribute: elem.attrib[attribute] for attribute in ['Id', 'ParentId', 'AcceptedAnswerId', 'CreationDate', 'LastEditDate', 'Body', 'LastEditorDisplayName', 'Title', 'Tags'] if attribute in elem.attrib})
    # lines.append(etree.tostring(new_row) + b'\x0a')
    my_bytes.extend(etree.tostring(new_row) + b'\x0a')
    # bytestring += etree.tostring(new_row) + b'\x0a'
    # f.write(etree.tostring(new_row) + b'\x0a')
    if i % 100000 == 0:
        f.write(my_bytes)
        # break
    if i == 1000000:
        break
